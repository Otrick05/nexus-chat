package com.example.nexuschat.nexuschat.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.nexuschat.nexuschat.DTO.request.ActualizarChatRequestDTO;
import com.example.nexuschat.nexuschat.DTO.request.CreateChatRequestDTO;
import com.example.nexuschat.nexuschat.DTO.request.EnviarMensajeRequestDTO;
import com.example.nexuschat.nexuschat.DTO.request.EnviarMensajeRequestDTO.ArchivoSolicitudDTO;
import com.example.nexuschat.nexuschat.DTO.response.ChatListResponseDTO;
import com.example.nexuschat.nexuschat.DTO.response.MensajeResponseDTO;
import com.example.nexuschat.nexuschat.DTO.response.MensajeResponseWBDTO;
import com.example.nexuschat.nexuschat.DTO.response.MensajeResumenDTO; // Keep despite warning if needed by other parts, or remove if truly unused. I'll include it to be safe, or remove if I want to be clean. I'll remove it as per lint.
import com.example.nexuschat.nexuschat.DTO.response.PerfilUsuarioDTO;
import com.example.nexuschat.nexuschat.model.Chat;
import com.example.nexuschat.nexuschat.model.Mensaje;
import com.example.nexuschat.nexuschat.model.Multimedia;
import com.example.nexuschat.nexuschat.model.ParticipanteChat;
import com.example.nexuschat.nexuschat.model.Usuario;
import com.example.nexuschat.nexuschat.repository.ChatRepository;
import com.example.nexuschat.nexuschat.repository.MensajeRepository;
import com.example.nexuschat.nexuschat.repository.MultimediaRepository;
import com.example.nexuschat.nexuschat.repository.ParticipanteChatRepository;
import com.example.nexuschat.nexuschat.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
@Slf4j
public class ChatGeneralService {

    private final ChatRepository chatRepository;
    private final UsuarioRepository usuarioRepository;
    private final ParticipanteChatRepository participanteChatRepository;
    private final MensajeRepository mensajeRepository;
    private final RealtimeChatService realtimeChatService;
    private final MultimediaRepository multimediaRepository;
    private final StorageService storageService;

    public ChatGeneralService(ChatRepository chatRepository,
            UsuarioRepository usuarioRepository,
            ParticipanteChatRepository participanteChatRepository,
            MensajeRepository mensajeRepository,
            RealtimeChatService realtimeChatService,
            MultimediaRepository multimediaRepository,
            StorageService storageService) {
        this.chatRepository = chatRepository;
        this.usuarioRepository = usuarioRepository;
        this.participanteChatRepository = participanteChatRepository;
        this.mensajeRepository = mensajeRepository;
        this.realtimeChatService = realtimeChatService;
        this.multimediaRepository = multimediaRepository;
        this.storageService = storageService;
    }

    @Transactional
    public ChatListResponseDTO crearChat(CreateChatRequestDTO request, String correoCreador) {

        // 1. Buscar al usuario creador
        Usuario creador = buscarUsuarioPorCorreo(correoCreador);

        if (request.getEmailsMiembros() == null || request.getEmailsMiembros().isEmpty()) {
            throw new IllegalArgumentException(
                    "Para un chat, se debe proveer al menos un miembro (distinto al creador).");
        }

        // Si es GRUPO, delegamos al método especializado
        if (request.getTipo() == Chat.TipoChat.GRUPO) {
            return crearGrupo(request, correoCreador);
        }

        // --- LÓGICA PARA CHAT INDIVIDUAL (PRIVADO) ---

        // Un chat INDIVIDUAL debe tener exactamente 1 miembro (además del creador)
        if (request.getEmailsMiembros().size() != 1) {
            throw new IllegalArgumentException(
                    "Un chat Privado debe tener exactamente un miembro (además del creador).");
        }
        String correoOtroUsuario = request.getEmailsMiembros().get(0);

        // Verificar si YA existe un chat privado entre estos dos
        Usuario otroUsuario = buscarUsuarioPorCorreo(correoOtroUsuario);

        // Buscar chat existente iterando los chats activos del creador
        Chat chatExistente = null;
        List<ParticipanteChat> chatsCreador = participanteChatRepository.findByUsuarioAndSalidaIsNull(creador);

        for (ParticipanteChat pc : chatsCreador) {
            Chat c = pc.getChat();
            if (c.getTipo() == Chat.TipoChat.PRIVADO) {
                // Verificar si el otro usuario es participante (activo o no)
                // Si es auto-chat (creador == otroUsuario), ya lo tenemos (pc.getUsuario() es
                // creador)
                if (creador.equals(otroUsuario)) {
                    chatExistente = c;
                    break;
                }

                // Si es chat con otro, buscamos su participación
                Optional<ParticipanteChat> otherPart = participanteChatRepository.findByChatAndUsuario(c, otroUsuario);
                if (otherPart.isPresent()) {
                    chatExistente = c;
                    break;
                }
            }
        }

        Chat chatGuardado;
        if (chatExistente != null) {
            chatGuardado = chatExistente;
            // Reactivar participantes si es necesario
            ensureActiveParticipation(chatGuardado, creador);
            if (!creador.equals(otroUsuario)) {
                ensureActiveParticipation(chatGuardado, otroUsuario);
            }
        } else {
            Chat nuevoChat = new Chat();
            // En privado, el nombre del chat para el creador puede ser el del otro usuario
            // Aunque generalmente en la lectura se resuelve dinámicamente.
            // Si es auto-chat (notas), el nombre podría ser "Mis Notas".
            if (correoCreador.equalsIgnoreCase(correoOtroUsuario)) {
                nuevoChat.setNombre("Mis Notas");
            } else {
                nuevoChat.setNombre(null); // Se resuelve al leer
            }

            nuevoChat.setCreacion(Instant.now());
            nuevoChat.setTipo(Chat.TipoChat.PRIVADO);

            chatGuardado = chatRepository.save(nuevoChat);

            // Crear participaciones
            crearParticipacion(chatGuardado, creador, ParticipanteChat.TipoUsuario.MIEMBRO);
            if (!correoCreador.equalsIgnoreCase(correoOtroUsuario)) {
                crearParticipacion(chatGuardado, otroUsuario, ParticipanteChat.TipoUsuario.MIEMBRO);
            }
        }

        // ** Lógica de Mensaje Inicial (si existe) **
        MensajeResponseDTO mensajeInicialEnviado = null;
        if (request.getMensajeInicial() != null && !request.getMensajeInicial().isBlank()) {
            EnviarMensajeRequestDTO mensajeDTO = new EnviarMensajeRequestDTO();
            mensajeDTO.setChatId(chatGuardado.getId());
            mensajeDTO.setContenido(request.getMensajeInicial());
            // Si viene tipoMensaje usamos ese, si no TEXTO por defecto
            mensajeDTO.setTipoMensaje(
                    request.getTipoMensaje() != null ? request.getTipoMensaje() : Mensaje.TipoMensaje.TEXTO);

            if (request.getArchivos() != null) {
                mensajeDTO.setArchivos(java.util.Arrays.asList(request.getArchivos()));
            }

            mensajeInicialEnviado = procesarYDifundirMensaje(mensajeDTO, correoCreador);
        }

        // Mapeo y Respuesta
        ChatListResponseDTO responseDTO = mapToChatListResponseDTO(chatGuardado, null, creador);

        // Notificación de nuevo chat
        if (!correoCreador.equalsIgnoreCase(correoOtroUsuario)) {
            realtimeChatService.notificarNuevoChat(correoOtroUsuario, responseDTO, mensajeInicialEnviado);
        }

        return responseDTO;
    }

    @Transactional
    public ChatListResponseDTO crearGrupo(CreateChatRequestDTO request, String correoCreador) {
        Usuario creador = buscarUsuarioPorCorreo(correoCreador);

        // Validación básica
        if (request.getEmailsMiembros().isEmpty()) {
            throw new IllegalArgumentException("Un grupo debe tener al menos un miembro invitado.");
        }

        Chat nuevoChat = new Chat();
        nuevoChat.setCreacion(Instant.now());
        nuevoChat.setTipo(Chat.TipoChat.GRUPO);
        // Nombre obligatorio o por defecto
        if (request.getNombreChat() == null || request.getNombreChat().isBlank()) {
            nuevoChat.setNombre("Grupo de " + creador.getNombreUsuario());
        } else {
            nuevoChat.setNombre(request.getNombreChat());
        }

        Chat chatGuardado = chatRepository.save(nuevoChat);

        // Creador es PROPIETARIO
        crearParticipacion(chatGuardado, creador, ParticipanteChat.TipoUsuario.PROPIETARIO);

        // Invitados son MIEMBROS
        for (String emailMiembro : request.getEmailsMiembros()) {
            if (emailMiembro.equalsIgnoreCase(correoCreador))
                continue;
            Usuario miembro = buscarUsuarioPorCorreo(emailMiembro);
            crearParticipacion(chatGuardado, miembro, ParticipanteChat.TipoUsuario.MIEMBRO);
        }

        ChatListResponseDTO responseDTO = mapToChatListResponseDTO(chatGuardado, null, creador);

        // ** Agregar "System Message" simulado al DTO de respuesta para UI **
        ChatListResponseDTO.MensajeResumenDTO fakeSystemMsg = new ChatListResponseDTO.MensajeResumenDTO();
        fakeSystemMsg.setContenido(creador.getNombreUsuario() + " te ha añadido al grupo");
        fakeSystemMsg.setRemitenteCorreo("sistema@nexuschat"); // O null
        fakeSystemMsg.setNombreRemitente("Sistema");
        fakeSystemMsg.setHora(Instant.now());
        fakeSystemMsg.setBorradoParaTodos(false);
        responseDTO.setUltimoMensaje(fakeSystemMsg);

        // Notificar a todos los invitados
        for (String emailMiembro : request.getEmailsMiembros()) {
            if (!emailMiembro.equalsIgnoreCase(correoCreador)) {
                // Enviamos el DTO modificado con el mensaje de sistema
                realtimeChatService.notificarNuevoChat(emailMiembro, responseDTO, null);
            }
        }

        return responseDTO;
    }

    private void crearParticipacion(Chat chat, Usuario usuario, ParticipanteChat.TipoUsuario tipo) {
        ParticipanteChat p = new ParticipanteChat();
        p.setChat(chat);
        p.setUsuario(usuario);
        p.setTipo(tipo);
        p.setIngreso(Instant.now());
        participanteChatRepository.save(p);
    }

    private void ensureActiveParticipation(Chat chat, Usuario usuario) {
        // Verificar si tiene participación activa
        boolean isActive = participanteChatRepository.findByChatAndUsuarioAndSalidaIsNull(chat, usuario).isPresent();
        if (!isActive) {
            // Si no está activo (nunca estuvo o se fue), crear nueva participación
            crearParticipacion(chat, usuario, ParticipanteChat.TipoUsuario.MIEMBRO);
        }
    }

    @Transactional
    public List<ChatListResponseDTO> obtenerChatsDeUsuario(String correoUsuario) {
        Usuario usuario = buscarUsuarioPorCorreo(correoUsuario);

        List<ParticipanteChat> participacionesActivas = participanteChatRepository
                .findByUsuarioAndSalidaIsNull(usuario);

        return participacionesActivas.stream()
                .map(participacion -> {
                    Chat chat = participacion.getChat();

                    // Aplicamos lógica de filtrado por 'fecha_ingreso' (HISTORIAL)
                    Instant fechaIngreso = participacion.getIngreso();

                    // Solo obtenemos el último mensaje SI es posterior al ingreso del usuario
                    Mensaje ultimoMensaje = mensajeRepository.findTopByChatOrderByHoraDesc(chat)
                            .orElse(null);

                    if (ultimoMensaje != null && ultimoMensaje.getHora().isBefore(fechaIngreso)) {
                        ultimoMensaje = null; // No mostrar mensajes anteriores al ingreso
                    }

                    return mapToChatListResponseDTO(chat, ultimoMensaje, usuario);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatListResponseDTO actualizarDetallesChat(Long idChat,
            ActualizarChatRequestDTO request,
            String correoAdmin) {
        Usuario admin = buscarUsuarioPorCorreo(correoAdmin);
        Chat chat = buscarChatPorId(idChat);

        // Validación de permisos
        validarPermisoAdmin(admin, chat);

        // Mapeo Manual (DTO -> Entidad)
        if (request.getNombreChat() != null) {
            chat.setNombre(request.getNombreChat());
        }
        if (request.getUrlImagen() != null) {
            chat.setImagenUrl(request.getUrlImagen());
        }

        Chat chatActualizado = chatRepository.save(chat);

        ChatListResponseDTO responseDTO = mapToChatListResponseDTO(chatActualizado, null, admin);

        // Notificar en tiempo real a los demás miembros
        realtimeChatService.difundirActualizacionChat(idChat, responseDTO);

        return responseDTO;
    }

    @Transactional
    public MensajeResponseDTO procesarYDifundirMensaje(EnviarMensajeRequestDTO dto, String correoRemitente) {
        Usuario remitente = buscarUsuarioPorCorreo(correoRemitente);
        Chat chat = buscarChatPorId(dto.getChatId());

        // 1. Validación de permisos
        ParticipanteChat participante = buscarParticipacionActiva(chat, remitente);
        if (participante.getTipo() == ParticipanteChat.TipoUsuario.ELIMINADO) {
            throw new SecurityException("No puedes enviar mensajes porque fuiste eliminado del grupo.");
        }

        // 2. Mapeo Manual (DTO -> Entidad Mensaje)
        log.info("Procesando mensaje. DTO recibido: {}", dto);
        System.out.println("DTO Archivos size: " + (dto.getArchivos() != null ? dto.getArchivos().size() : "null"));

        Mensaje nuevoMensaje = new Mensaje();
        nuevoMensaje.setChat(chat);
        nuevoMensaje.setRemitente(remitente);
        nuevoMensaje.setHora(Instant.now());
        nuevoMensaje.setBorradoTodos(false);
        nuevoMensaje.setTipo(dto.getTipoMensaje());
        nuevoMensaje.setContenido(dto.getContenido());

        Mensaje mensajeGuardado = mensajeRepository.save(nuevoMensaje);

        // 3. Mapeo Manual (DTO -> Entidad Multimedia) y Generación de Signed URLs
        List<Multimedia> multimediaGuardada = new ArrayList<>();
        List<String> uploadUrls = new ArrayList<>(); // Para guardar las URLs firmadas temporalmente

        if (dto.getArchivos() != null && !dto.getArchivos().isEmpty()) {
            log.info("Procesando {} archivos adjuntos.", dto.getArchivos().size());
            for (ArchivoSolicitudDTO archivoDTO : dto
                    .getArchivos()) {

                // Generar nombre único para el archivo en GCS SI NO VIENE PRE-FIRMADO
                String uniqueFileName;
                String signedUrl = null;

                if (archivoDTO.getFileName() != null && !archivoDTO.getFileName().isBlank()) {
                    // Flujo Nuevo: Archivo ya subido (pre-signed)
                    uniqueFileName = archivoDTO.getFileName();
                    // No generamos uploadUrl, el signedUrl es null porque ya se subió
                } else {
                    // Flujo Legacy: Generamos URL de subida AHORA
                    uniqueFileName = java.util.UUID.randomUUID().toString() + "_" + archivoDTO.getNombreArchivo();
                    signedUrl = storageService.generarUrlFirmada(uniqueFileName, archivoDTO.getContentType());
                }

                uploadUrls.add(signedUrl); // Puede agregar null, el mapper lo debe manejar (o el front saber que null =
                                           // ya subido)

                Multimedia media = new Multimedia();
                media.setMensaje(mensajeGuardado);
                // Guardamos el key del storage (nombre del archivo)
                media.setUrl_storage(uniqueFileName);

                // Determinar tipo de multimedia basado en el mensaje o contentType
                // (simplificado)
                if (dto.getTipoMensaje() == Mensaje.TipoMensaje.FOTO)
                    media.setTipo(Multimedia.TipoMultimedia.FOTO);
                else if (dto.getTipoMensaje() == Mensaje.TipoMensaje.VIDEO)
                    media.setTipo(Multimedia.TipoMultimedia.VIDEO);
                else if (dto.getTipoMensaje() == Mensaje.TipoMensaje.AUDIO)
                    media.setTipo(Multimedia.TipoMultimedia.AUDIO);
                else
                    media.setTipo(Multimedia.TipoMultimedia.FOTO); // Default

                media.setBytes_size(archivoDTO.getTamanoBytes());

                if (archivoDTO.getDuracion() != null) {
                    try {
                        media.setDuracion(java.time.Duration.parse(archivoDTO.getDuracion()));
                    } catch (Exception e) {
                        // Ignore
                    }
                }
                multimediaGuardada.add(multimediaRepository.save(media));
            }
        }

        // 3.5 Generar URLs firmadas de LECTURA para que el front pueda mostrar el
        // archivo inmediatamente
        List<String> readUrls = new ArrayList<>();
        for (Multimedia media : multimediaGuardada) {
            String readUrl = storageService.generarUrlFirmadaLectura(media.getUrl_storage());
            readUrls.add(readUrl);
        }

        // 4. Mapeo Manual (Entidad -> DTO de Respuesta)
        // Pasamos las uploadUrls y readUrls al mapper
        MensajeResponseDTO responseDTO = mapToMensajeResponseDTO(mensajeGuardado, multimediaGuardada, uploadUrls,
                readUrls);

        // 5. Difundir a todos los miembros del chat
        // Obtener lista de emails de participantes activos
        List<String> emailsDestino = participanteChatRepository.findByChatAndSalidaIsNull(chat)
                .stream()
                .map(p -> p.getUsuario().getCorreo())
                .collect(Collectors.toList());

        MensajeResponseWBDTO lightDTO = mapToMensajeResponseWBDTO(mensajeGuardado);

        realtimeChatService.notificarMensaje(responseDTO, lightDTO, emailsDestino);

        return responseDTO;
    }

    @Transactional
    public Page<MensajeResponseDTO> obtenerMensajesDeChat(Long idChat,
            String emailUsuario, Pageable pageable) {
        Usuario usuario = buscarUsuarioPorCorreo(emailUsuario);
        Chat chat = buscarChatPorId(idChat);

        // Validar que el usuario pertenece al chat y obtener su ultima participacion
        // activa
        ParticipanteChat participacion = buscarParticipacionActiva(chat, usuario);

        // Si el usuario está ELIMINADO, solo puede ver mensajes hasta su fecha de
        // eliminación
        Instant fechaTope = Instant.now();
        if (participacion.getTipo() == ParticipanteChat.TipoUsuario.ELIMINADO) {
            if (participacion.getFechaEliminacion() != null) {
                fechaTope = participacion.getFechaEliminacion();
            }
        }

        // Filtrar mensajes posteriores a su ingreso y anteriores a su eliminación (si
        // aplica)
        Page<Mensaje> mensajes;
        if (participacion.getTipo() == ParticipanteChat.TipoUsuario.ELIMINADO) {
            // Messages between ingreso and fechaEliminacion
            mensajes = mensajeRepository.findByChatAndHoraBetween(chat,
                    participacion.getIngreso(), fechaTope, pageable);
        } else {
            // Active members: from ingreso onwards
            mensajes = mensajeRepository.findByChatAndHoraAfter(chat,
                    participacion.getIngreso(), pageable);
        }

        return mensajes.map(mensaje -> {
            List<Multimedia> multimedia = multimediaRepository.findByMensaje(mensaje);

            // Generar URLs firmadas de lectura para cada multimedia
            List<String> downloadUrls = new ArrayList<>();
            for (Multimedia m : multimedia) {
                // Generamos la URL firmada de lectura usando el nombre del archivo guardado
                // (url_storage)
                String signedUrl = storageService.generarUrlFirmadaLectura(m.getUrl_storage());
                downloadUrls.add(signedUrl);
            }

            // Pasamos las URLs de descarga al mapper como readUrls
            MensajeResponseDTO responseDTO = mapToMensajeResponseDTO(mensaje, multimedia, null, downloadUrls);

            log.info("Chat: {} | MsgID: {} | URLs: {}", idChat, responseDTO.getId(), downloadUrls);

            return responseDTO;
        });
    }

    @Transactional
    public void agregarParticipante(Long idChat, String correoAdmin, String correoNuevoMiembro) {
        Usuario admin = buscarUsuarioPorCorreo(correoAdmin);
        Usuario nuevoMiembro = buscarUsuarioPorCorreo(correoNuevoMiembro);
        Chat chat = buscarChatPorId(idChat);

        // 1. Validación de permisos
        validarPermisoAdmin(admin, chat);

        // 2. Lógica de negocio
        // Verificar si el usuario ya está activo
        Optional<ParticipanteChat> participacionExistente = participanteChatRepository
                .findByChatAndUsuarioAndSalidaIsNull(chat,
                        nuevoMiembro);

        if (participacionExistente.isPresent()) {
            ParticipanteChat p = participacionExistente.get();
            if (p.getTipo() == ParticipanteChat.TipoUsuario.ELIMINADO) {
                // Reactivar usuario eliminado
                p.setTipo(ParticipanteChat.TipoUsuario.MIEMBRO);
                p.setFechaEliminacion(null);
                // Opcional: actualizar fecha de ingreso si queremos que solo vea nuevos
                // mensajes desde ahora?
                // El usuario "vuelve" a entrar. Generalmente en apps como WA, al volver a
                // entrar
                // ves historial si no lo borraste, pero aquí validamos por fecha ingreso.
                // Si mantenemos fecha ingreso original, ve todo el historial.
                // Si actualizamos fecha ingreso, ve desde ahora.
                // Asumiremos reset de ingreso para consistencia con "nuevo miembro"
                p.setIngreso(Instant.now());

                participanteChatRepository.save(p);
                return; // Terminado
            } else {
                throw new IllegalStateException("El usuario ya es miembro activo de este chat.");
            }
        }

        // 3. Crear nueva participación
        ParticipanteChat nuevaParticipacion = new ParticipanteChat();
        nuevaParticipacion.setChat(chat);
        nuevaParticipacion.setUsuario(nuevoMiembro);
        nuevaParticipacion.setTipo(ParticipanteChat.TipoUsuario.MIEMBRO);
        nuevaParticipacion.setIngreso(Instant.now());
        nuevaParticipacion.setSalida(null);

        participanteChatRepository.save(nuevaParticipacion);

        // 4. Notificar en tiempo real
        // realtimeChatService.difundirCambioDeParticipante(idChat,correoNuevoMiembro,"USER_JOINED");
    }

    @Transactional
    public void expulsarParticipante(Long idChat, String correoAdmin, Long idUsuarioAExpulsar) {
        Usuario admin = buscarUsuarioPorCorreo(correoAdmin);
        Usuario usuarioAExpulsar = buscarUsuarioPorId(idUsuarioAExpulsar);
        Chat chat = buscarChatPorId(idChat);

        // 1. Validación de permisos
        validarPermisoAdmin(admin, chat);

        // No puedes expulsarte a ti mismo (usa /me/leave)
        if (admin.getId().equals(idUsuarioAExpulsar)) {
            throw new IllegalArgumentException("No puedes expulsarte a ti mismo.");
        }

        // 2. Lógica de negocio
        ParticipanteChat participacion = buscarParticipacionActiva(chat,
                usuarioAExpulsar);

        // No puedes expulsar a un PROPIETARIO (solo él puede transferir)
        /*
         * if (participacion.getTipoUsuario() == ParticipanteChat.RolChat.PROPIETARIO){
         * throw
         * newIllegalStateException("No se puede expulsar al propietario del chat.");
         * }
         */

        // Marcar como ELIMINADO para mantener historial pero revocar acceso
        participacion.setTipo(ParticipanteChat.TipoUsuario.ELIMINADO);
        participacion.setFechaEliminacion(Instant.now());
        // No seteamos salida a now() para que siga "estando" en el chat y pueda verlo
        // en su lista
        // participacion.setSalida(Instant.now());
        participanteChatRepository.save(participacion);

        // 3. Notificar en tiempo real
        // realtimeChatService.difundirCambioDeParticipante(idChat,usuarioAExpulsar.getEmail(),"USER_LEFT");

    }

    @Transactional
    public void salirDeChat(Long idChat, String correoUsuario) {
        Usuario usuario = buscarUsuarioPorCorreo(correoUsuario);
        Chat chat = buscarChatPorId(idChat);

        // 1. Lógica de negocio
        ParticipanteChat participacion = buscarParticipacionActiva(chat, usuario);
        participacion.setSalida(Instant.now());

        // 2. Lógica de Transferencia de Propiedad
        if (participacion.getTipo() == ParticipanteChat.TipoUsuario.PROPIETARIO) {
            gestionarTransferenciaPropiedadAutomatica(chat);
        }

        participanteChatRepository.save(participacion);

        // 3. Notificar en tiempo real
        // realtimeChatService.difundirCambioDeParticipante(idChat, emailUsuario,
        // "USER_LEFT");
    }

    @Transactional
    public void transferirPropiedad(Long idChat, String correoPropietario, String correoNuevoPropietario) {
        Usuario propietarioActual = buscarUsuarioPorCorreo(correoPropietario);
        Usuario nuevoPropietario = buscarUsuarioPorCorreo(correoNuevoPropietario);
        Chat chat = buscarChatPorId(idChat);

        // 1. Validación de permisos
        ParticipanteChat participacionActual = validarPermisoPropietario(propietarioActual, chat);

        // 2. Lógica de negocio
        ParticipanteChat participacionNueva = buscarParticipacionActiva(chat,
                nuevoPropietario);

        if (participacionActual.getId().equals(participacionNueva.getId())) {
            return; // Ya es el propietario
        }

        participacionActual.setTipo(ParticipanteChat.TipoUsuario.ADMIN_GRUPO); //
        // Degradar al propietario actual
        participacionNueva.setTipo(ParticipanteChat.TipoUsuario.PROPIETARIO); //
        // Ascender al nuevo
        participanteChatRepository.save(participacionActual);
        participanteChatRepository.save(participacionNueva);

        // 3. Notificar en tiempo real
        /*
         * realtimeChatService.difundirCambioDeRol(idChat, nuevoPropietario.getCorreo(),
         * propietarioActual.getCorreo());
         */
    }

    private void gestionarTransferenciaPropiedadAutomatica(Chat chat) {
        // 1. Buscar al Admin más antiguo (que sigue activo)
        participanteChatRepository.findFirstByChatAndTipoAndSalidaIsNullOrderByIngresoAsc(
                chat, ParticipanteChat.TipoUsuario.ADMIN_GRUPO)
                .ifPresentOrElse(
                        // 2. Si se encuentra, ascenderlo a PROPIETARIO
                        adminMasAntiguo -> {
                            adminMasAntiguo.setTipo(ParticipanteChat.TipoUsuario.PROPIETARIO);
                            participanteChatRepository.save(adminMasAntiguo);
                        },
                        // 3. Si no hay Admins, buscar al Miembro más antiguo (que sigue activo)
                        () -> participanteChatRepository.findFirstByChatAndTipoAndSalidaIsNullOrderByIngresoAsc(
                                chat, ParticipanteChat.TipoUsuario.MIEMBRO)
                                .ifPresent(miembroMasAntiguo -> {
                                    miembroMasAntiguo.setTipo(ParticipanteChat.TipoUsuario.PROPIETARIO);
                                    participanteChatRepository.save(miembroMasAntiguo);
                                }));
    }

    // --- MÉTODOS PRIVADOS DE BÚSQUEDA Y VALIDACIÓN ---

    private Usuario buscarUsuarioPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con correo: " + correo));
    }

    private Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
    }

    private Chat buscarChatPorId(Long idChat) {
        return chatRepository.findById(idChat)
                .orElseThrow(() -> new EntityNotFoundException("Chat no encontrado con id: " + idChat));
    }

    @Transactional
    public List<PerfilUsuarioDTO> obtenerParticipantesChat(Long chatId, String emailSolicitante) {
        Chat chat = buscarChatPorId(chatId);
        Usuario solicitante = buscarUsuarioPorCorreo(emailSolicitante);

        // Validar que el solicitante sea miembro del chat
        buscarParticipacionActiva(chat, solicitante);

        // Obtener participantes activos
        return participanteChatRepository.findByChatAndSalidaIsNull(chat)
                .stream()
                .map(p -> {
                    Usuario u = p.getUsuario();
                    return PerfilUsuarioDTO.builder()
                            .nombreUsuario(u.getNombreUsuario())
                            .correo(u.getCorreo())
                            .nombreAppUsuario(u.getNombreAppUsuario())
                            .avatarUrl(u.getAvatarUrl())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private ParticipanteChat buscarParticipacionActiva(Chat chat, Usuario usuario) {
        // CORREGIDO: Usando findByChatAndUsuarioAndSalidaIsNull
        return participanteChatRepository.findByChatAndUsuarioAndSalidaIsNull(chat, usuario)
                .orElseThrow(() -> new EntityNotFoundException("El usuario no es miembro activo de este chat."));
    }

    private void validarPermisoAdmin(Usuario usuario, Chat chat) {
        ParticipanteChat p = buscarParticipacionActiva(chat, usuario);
        if (p.getTipo() != ParticipanteChat.TipoUsuario.ADMIN_GRUPO
                &&
                p.getTipo() != ParticipanteChat.TipoUsuario.PROPIETARIO) {
            throw new SecurityException("Permiso denegado. Se requiere rol de ADMIN o PROPIETARIO.");
        }
    }

    private ParticipanteChat validarPermisoPropietario(Usuario usuario, Chat chat) {
        ParticipanteChat p = buscarParticipacionActiva(chat, usuario);
        if (p.getTipo() != ParticipanteChat.TipoUsuario.PROPIETARIO) {
            throw new SecurityException("Permiso denegado. Se requiere rol de PROPIETARIO.");
        }
        return p;
    }

    // --- MÉTODOS PRIVADOS DE MAPEO MANUAL ---

    private ChatListResponseDTO mapToChatListResponseDTO(Chat chat, Mensaje ultimoMensaje, Usuario usuarioActual) {

        ChatListResponseDTO.MensajeResumenDTO mensajeResumen = (ultimoMensaje == null)
                ? null
                : mapToMensajeResumenDTO(ultimoMensaje);

        String nombreChat = chat.getNombre();
        String urlImagen = chat.getImagenUrl();

        // Lógica para chats individuales: mostrar el nombre y foto del *otro* usuario
        if (chat.getTipo() == Chat.TipoChat.PRIVADO) {
            ParticipanteChat otroParticipante = participanteChatRepository
                    .findByChatAndUsuarioNotAndSalidaIsNull(chat, usuarioActual)
                    .orElse(null); // Puede ser nulo si el otro se fue

            if (otroParticipante != null) {
                nombreChat = otroParticipante.getUsuario().getNombreUsuario();
                urlImagen = otroParticipante.getUsuario().getAvatarUrl();
            }
        }

        // Logic to populate participants list
        List<ChatListResponseDTO.ParticipanteDTO> participantesDTO = new ArrayList<>();
        List<ParticipanteChat> participantes = participanteChatRepository.findByChatAndSalidaIsNull(chat);

        for (ParticipanteChat p : participantes) {
            ChatListResponseDTO.ParticipanteDTO pdto = new ChatListResponseDTO.ParticipanteDTO();
            pdto.setIdUsuario(p.getUsuario().getId());
            pdto.setCorreo(p.getUsuario().getCorreo());
            pdto.setNombreUsuario(p.getUsuario().getNombreUsuario());
            pdto.setAvatarUrl(p.getUsuario().getAvatarUrl());
            pdto.setRol(p.getTipo().name());
            participantesDTO.add(pdto);
        }

        ChatListResponseDTO dto = new ChatListResponseDTO();
        dto.setIdChat(chat.getId());
        dto.setTipo(chat.getTipo().name());
        dto.setNombreChat(nombreChat);
        dto.setUrlAvatar(urlImagen);
        dto.setUltimoMensaje(mensajeResumen);
        // dto.setConteoNoLeidos(0); // Implementación futura
        dto.setParticipantes(participantesDTO); // Nuevo campo

        return dto;
    }

    private ChatListResponseDTO.MensajeResumenDTO mapToMensajeResumenDTO(Mensaje mensaje) {
        ChatListResponseDTO.MensajeResumenDTO dto = new ChatListResponseDTO.MensajeResumenDTO();

        // Lógica para mostrar "Imagen", "Video" o "Audio"
        if (mensaje.getTipo() == Mensaje.TipoMensaje.AUDIO
                || mensaje.getTipo() == Mensaje.TipoMensaje.FOTO
                || mensaje.getTipo() == Mensaje.TipoMensaje.VIDEO) {
            dto.setContenido("Archivo multimedia");
        } else {
            dto.setContenido(mensaje.getContenido());
        }

        dto.setRemitenteCorreo(mensaje.getRemitente().getCorreo());
        // dto.setNombreRemitente(mensaje.getRemitente().getNombreUsuario());
        // Nota: MensajeResumenDTO interno no tiene nombreRemitente en algunas
        // versiones,
        // pero el usuario lo agregó en Step 326?
        // Step 326: public static class MensajeResumenDTO { ... private String
        // nombreRemitente; ... }
        // Así que sí lo tiene.
        dto.setNombreRemitente(mensaje.getRemitente().getNombreUsuario());
        dto.setHora(mensaje.getHora());
        dto.setBorradoParaTodos(mensaje.getBorradoTodos());
        return dto;
    }

    private MensajeResponseDTO mapToMensajeResponseDTO(Mensaje mensaje, List<Multimedia> multimedia,
            List<String> uploadUrls, List<String> readUrls) {
        MensajeResponseDTO.RemitenteDTO remitenteDTO = new MensajeResponseDTO.RemitenteDTO();
        remitenteDTO.setId(mensaje.getRemitente().getId());
        remitenteDTO.setCorreo(mensaje.getRemitente().getCorreo());
        remitenteDTO.setNombreUsuario(mensaje.getRemitente().getNombreUsuario());
        remitenteDTO.setAvatarUrl(mensaje.getRemitente().getAvatarUrl());

        // Usamos un índice para asignar la uploadUrl correspondiente a cada multimedia
        // Asumimos que el orden de la lista 'multimedia' coincide con 'uploadUrls' si
        // esta última no es nula
        List<MensajeResponseDTO.MultimediaResponseDTO> multimediaDTOs = new ArrayList<>();
        for (int i = 0; i < multimedia.size(); i++) {
            Multimedia m = multimedia.get(i);
            MensajeResponseDTO.MultimediaResponseDTO mdto = new MensajeResponseDTO.MultimediaResponseDTO();
            mdto.setId(m.getId());

            // Si pasamos readUrls (URLs firmadas de lectura), las usamos. Si no, usamos el
            // fallback (nombre archivo)
            if (readUrls != null && i < readUrls.size()) {
                mdto.setUrlStorage(readUrls.get(i));
            } else {
                mdto.setUrlStorage(m.getUrl_storage());
            }

            mdto.setTipo(m.getTipo().name());
            mdto.setTamañoBytes(m.getBytes_size());
            mdto.setDuracion(m.getDuracion() != null ? m.getDuracion().toString() : null);
            mdto.setOrden(0);

            if (uploadUrls != null && i < uploadUrls.size()) {
                mdto.setUploadUrl(uploadUrls.get(i));
            }

            multimediaDTOs.add(mdto);
        }

        return MensajeResponseDTO.builder()
                .id(mensaje.getId())
                .chatId(mensaje.getChat().getId())
                .contenido(mensaje.getContenido())
                .hora(mensaje.getHora())
                .borradoParaTodos(mensaje.getBorradoTodos())
                .tipoMensaje(mensaje.getTipo().name())
                .remitente(remitenteDTO)
                .multimedia(multimediaDTOs)
                .build();
    }

    private MensajeResponseWBDTO mapToMensajeResponseWBDTO(Mensaje mensaje) {
        MensajeResponseWBDTO dto = new MensajeResponseWBDTO();
        dto.setId(mensaje.getId());
        dto.setChatId(mensaje.getChat().getId());

        // Lógica para tipo de contenido (similar a MensajeResumenDTO)
        if (mensaje.getTipo() == Mensaje.TipoMensaje.AUDIO
                || mensaje.getTipo() == Mensaje.TipoMensaje.FOTO
                || mensaje.getTipo() == Mensaje.TipoMensaje.VIDEO) {
            dto.setContenido("Archivo multimedia");
        } else {
            dto.setContenido(mensaje.getContenido());
        }

        dto.setTipoMensaje(mensaje.getTipo().name());
        dto.setHora(mensaje.getHora());
        dto.setRemitenteEmail(mensaje.getRemitente().getCorreo());
        dto.setRemitenteNombre(mensaje.getRemitente().getNombreUsuario());

        return dto;
    }
}
