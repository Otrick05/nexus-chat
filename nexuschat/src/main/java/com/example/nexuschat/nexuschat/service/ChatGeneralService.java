package com.example.nexuschat.nexuschat.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.nexuschat.nexuschat.DTO.request.ActualizarChatRequestDTO;
import com.example.nexuschat.nexuschat.DTO.request.ChatMessageDTO;
import com.example.nexuschat.nexuschat.DTO.request.CreateChatRequestDTO;
import com.example.nexuschat.nexuschat.DTO.request.EnviarMensajeRequestDTO;
import com.example.nexuschat.nexuschat.DTO.request.EnviarMensajeRequestDTO.ArchivoSolicitudDTO;
import com.example.nexuschat.nexuschat.DTO.response.ChatListResponseDTO;
import com.example.nexuschat.nexuschat.DTO.response.MensajeResponseDTO;
import com.example.nexuschat.nexuschat.DTO.response.MensajeResumenDTO;
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

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
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

        // 2. Mapeo Manual (DTO -> Entidad)
        Chat nuevoChat = new Chat();
        nuevoChat.setNombre(request.getNombreChat());
        nuevoChat.setCreacion(Instant.now());
        Chat.TipoChat tipoChatSolicitado = request.getTipo();
        nuevoChat.setTipo(tipoChatSolicitado);

        if (tipoChatSolicitado == Chat.TipoChat.PRIVADO) {
            // Un chat INDIVIDUAL debe tener exactamente 1 miembro (además del creador)
            if (request.getEmailsMiembros().size() != 1) {
                throw new IllegalArgumentException(
                        "Un chat Privado debe tener exactamente un miembro (además del creador).");
            }
            // Validar que no intente crear un chat consigo mismo
            if (request.getEmailsMiembros().get(0).equalsIgnoreCase(correoCreador)) {
                throw new IllegalArgumentException("No puedes crear un chat individual contigo mismo.");
            }

            if (request.getEmailsMiembros().isEmpty()) {
                throw new IllegalArgumentException(
                        "Para un chat individual, se debe proveer el email del otro miembro.");
            }
        }

        ParticipanteChat participacionCreador = new ParticipanteChat();
        // Lógica para chats individuales o grupales
        if (tipoChatSolicitado == Chat.TipoChat.PRIVADO && request.getNombreChat() == null) {

            Usuario otroUsuario = buscarUsuarioPorCorreo(request.getEmailsMiembros().get(0));
            nuevoChat.setNombre(otroUsuario.getNombreUsuario());
            participacionCreador.setTipo(ParticipanteChat.TipoUsuario.MIEMBRO);
        } else if (tipoChatSolicitado == Chat.TipoChat.GRUPO) {
            if (request.getNombreChat() == null || request.getNombreChat().isBlank()) {
                nuevoChat.setNombre("Nuevo Grupo"); // Opcional: nombre por defecto
            } else {
                nuevoChat.setNombre(request.getNombreChat());
            }
            participacionCreador.setTipo(ParticipanteChat.TipoUsuario.PROPIETARIO);
        }

        Chat chatGuardado = chatRepository.save(nuevoChat);

        // 3. Crear y guardar la participación del Creador

        participacionCreador.setChat(chatGuardado);
        participacionCreador.setUsuario(creador);
        participacionCreador.setIngreso(Instant.now());
        participacionCreador.setSalida(null); // Participante activo
        participanteChatRepository.save(participacionCreador);

        // 4. Crear y guardar la participación de los otros miembros
        for (String emailMiembro : request.getEmailsMiembros()) {
            Usuario miembro = buscarUsuarioPorCorreo(emailMiembro);

            // Asegurarnos de no añadir al creador dos veces (si se incluyó en la lista)
            if (miembro.getId().equals(creador.getId()))
                continue;

            ParticipanteChat participacionMiembro = new ParticipanteChat();
            participacionMiembro.setChat(chatGuardado);
            participacionMiembro.setUsuario(miembro);
            participacionMiembro.setTipo(ParticipanteChat.TipoUsuario.MIEMBRO);
            participacionMiembro.setIngreso(Instant.now());
            participacionMiembro.setSalida(null);
            participanteChatRepository.save(participacionMiembro);
        }

        // 5. Mapeo Manual (Entidad -> DTO de Respuesta)
        return mapToChatListResponseDTO(chatGuardado, null, creador);
    }

    @Transactional
    public List<ChatListResponseDTO> obtenerChatsDeUsuario(String correoUsuario) {
        Usuario usuario = buscarUsuarioPorCorreo(correoUsuario);

        List<ParticipanteChat> participacionesActivas = participanteChatRepository
                .findByUsuarioAndSalidaIsNull(usuario);

        return participacionesActivas.stream()
                .map(participacion -> {
                    Chat chat = participacion.getChat();

                    // Buscar el último mensaje de este chat
                    // (Aquí se aplicaría la lógica de filtrado por 'fecha_ingreso' y
                    // 'mensaje_estado_usuario')
                    Mensaje ultimoMensaje = mensajeRepository.findTopByChatOrderByHoraDesc(chat)
                            .orElse(null); // Es nulo si no hay mensajes

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
            chat.setImagen(request.getUrlImagen());
        }

        Chat chatActualizado = chatRepository.save(chat);

        ChatListResponseDTO responseDTO = mapToChatListResponseDTO(chatActualizado, null, admin);

        // Notificar en tiempo real a los demás miembros
        // realtimeChatService.difundirActualizacionChat(idChat, responseDTO);

        return responseDTO;
    }

    @Transactional
    public MensajeResponseDTO procesarYDifundirMensaje(EnviarMensajeRequestDTO dto, String correoRemitente) {
        Usuario remitente = buscarUsuarioPorCorreo(correoRemitente);
        Chat chat = buscarChatPorId(dto.getChatId());

        // 1. Validación de permisos
        ParticipanteChat participante = buscarParticipacionActiva(chat, remitente);

        // 2. Mapeo Manual (DTO -> Entidad Mensaje)
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
            for (ArchivoSolicitudDTO archivoDTO : dto
                    .getArchivos()) {

                // Generar nombre único para el archivo en GCS
                String uniqueFileName = java.util.UUID.randomUUID().toString() + "_" + archivoDTO.getNombreArchivo();

                // Generar URL firmada
                String signedUrl = storageService.generarUrlFirmada(uniqueFileName, archivoDTO.getContentType());
                uploadUrls.add(signedUrl);

                Multimedia media = new Multimedia();
                media.setMensaje(mensajeGuardado);
                // Guardamos la URL pública futura (sin firma) o el path relativo
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

        // 4. Mapeo Manual (Entidad -> DTO de Respuesta)
        // Pasamos las uploadUrls al mapper para que las incluya en la respuesta
        MensajeResponseDTO responseDTO = mapToMensajeResponseDTO(mensajeGuardado, multimediaGuardada, uploadUrls);

        // 5. Difundir a todos los miembros del chat (WebSocket comentado)
        // String destination = "/topic/chat/" + chat.getId();
        // messagingTemplate.convertAndSend(destination, responseDTO);

        return responseDTO;
    }

    @Transactional
    public List<MensajeResponseDTO> obtenerMensajesDeChat(Long idChat, String emailUsuario) {
        Usuario usuario = buscarUsuarioPorCorreo(emailUsuario);
        Chat chat = buscarChatPorId(idChat);

        // Validar que el usuario pertenece al chat
        buscarParticipacionActiva(chat, usuario);

        // Asumiendo que existe este método en el repositorio. Si no, habrá que crearlo.
        List<Mensaje> mensajes = mensajeRepository.findByChatOrderByHoraAsc(chat);

        return mensajes.stream().map(mensaje -> {
            List<Multimedia> multimedia = multimediaRepository.findByMensaje(mensaje);

            // Generar URLs firmadas de lectura para cada multimedia
            List<String> downloadUrls = new ArrayList<>();
            for (Multimedia m : multimedia) {
                // Generamos la URL firmada de lectura usando el nombre del archivo guardado
                // (url_storage)
                String signedUrl = storageService.generarUrlFirmadaLectura(m.getUrl_storage());
                downloadUrls.add(signedUrl);
            }

            // Pasamos las URLs de descarga al mapper.
            // NOTA: Reutilizamos el parámetro 'uploadUrls' del mapper para pasar las
            // 'downloadUrls'
            // ya que en el contexto de lectura, lo que queremos mostrar en 'urlStorage' o
            // un campo similar es la URL accesible.
            // Sin embargo, el mapper actual asigna 'uploadUrls' a 'uploadUrl' (para PUT).
            // Para lectura (GET), deberíamos asignar esta URL firmada a 'urlStorage' del
            // DTO o a un nuevo campo 'downloadUrl'.
            // Dado que el frontend probablemente usa 'urlStorage' para visualizar, vamos a
            // sobreescribir ese campo en el DTO
            // o modificar el mapper para que acepte 'downloadUrls'.

            // Modificaremos el mapper para aceptar 'downloadUrls' explícitamente o
            // manejaremos la lógica aquí.
            // Para no romper el mapper existente, lo llamamos con null en uploadUrls y
            // luego actualizamos los DTOs.

            MensajeResponseDTO responseDTO = mapToMensajeResponseDTO(mensaje, multimedia, null);

            // Actualizar urlStorage con la URL firmada de lectura
            List<MensajeResponseDTO.MultimediaResponseDTO> mediaDTOs = responseDTO.getMultimedia();
            for (int i = 0; i < mediaDTOs.size(); i++) {
                if (i < downloadUrls.size()) {
                    mediaDTOs.get(i).setUrlStorage(downloadUrls.get(i));
                }
            }

            return responseDTO;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void agregarParticipante(Long idChat, String correoAdmin, String correoNuevoMiembro) {
        Usuario admin = buscarUsuarioPorCorreo(correoAdmin);
        Usuario nuevoMiembro = buscarUsuarioPorCorreo(correoNuevoMiembro);
        Chat chat = buscarChatPorId(idChat);

        // 1. Validación de permisos
        validarPermisoAdmin(admin, chat);

        // 2. Lógica de negocio
        // Opcional: Verificar si el usuario ya está activo
        participanteChatRepository.findByChatAndUsuarioAndSalidaIsNull(chat,
                nuevoMiembro)
                .ifPresent(p -> {
                    throw new IllegalStateException("El usuario ya es miembro activo de este chat.");
                });

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

        // Marcar como inactivo (borrado lógico)
        // participacion.setSalida(LocalDateTime.now());
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
                                })
                // Si no hay nadie más, el chat queda sin propietario (lógica a definir: ¿se
                // borra?)
                );
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

    private ParticipanteChat buscarParticipacionActiva(Chat chat, Usuario usuario) {
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

        MensajeResumenDTO mensajeResumen = (ultimoMensaje == null)
                ? null
                : mapToMensajeResumenDTO(ultimoMensaje);

        String nombreChat = chat.getNombre();
        String urlImagen = chat.getImagen();

        // Lógica para chats individuales: mostrar el nombre y foto del *otro* usuario
        if (chat.getTipo() == Chat.TipoChat.PRIVADO) {
            ParticipanteChat otroParticipante = participanteChatRepository
                    .findByChatAndTipoNotAndSalidaIsNull(chat, usuarioActual)
                    .orElse(null); // Puede ser nulo si el otro se fue

            if (otroParticipante != null) {
                nombreChat = otroParticipante.getUsuario().getNombreUsuario(); // Asumiendo getNombreUsuario
                urlImagen = otroParticipante.getUsuario().getAvatarUrl(); // Asumiendo getAvatarUrl
            }
        }

        ChatListResponseDTO dto = new ChatListResponseDTO();
        dto.setIdChat(chat.getId());
        dto.setTipo(chat.getTipo().name());
        dto.setNombreChat(nombreChat);
        dto.setUrlAvatar(urlImagen);
        // dto.setUltimoMensaje(mensajeResumen);
        // dto.setConteoNoLeidos(0); // Implementación futura

        return dto;
    }

    private MensajeResumenDTO mapToMensajeResumenDTO(Mensaje mensaje) {
        MensajeResumenDTO dto = new MensajeResumenDTO();

        // Lógica para mostrar "Imagen", "Video" o "Audio" en lugar de contenido
        if (mensaje.getTipo() == Mensaje.TipoMensaje.AUDIO
                || mensaje.getTipo() == Mensaje.TipoMensaje.FOTO
                || mensaje.getTipo() == Mensaje.TipoMensaje.VIDEO) {
            // Esta lógica se puede mejorar para ser más específica (ej. "Foto")
            dto.setContenido("Archivo multimedia");
        } else {
            dto.setContenido(mensaje.getContenido());
        }

        dto.setRemitenteEmail(mensaje.getRemitente().getCorreo());
        dto.setHora(mensaje.getHora());
        dto.setBorradoTodos(mensaje.getBorradoTodos());
        return dto;
    }

    private MensajeResponseDTO mapToMensajeResponseDTO(Mensaje mensaje, List<Multimedia> multimedia,
            List<String> uploadUrls) {
        MensajeResponseDTO.RemitenteDTO remitenteDTO = new MensajeResponseDTO.RemitenteDTO();
        remitenteDTO.setId(mensaje.getRemitente().getId());
        remitenteDTO.setCorreo4(mensaje.getRemitente().getCorreo());
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
            mdto.setUrlStorage(m.getUrl_storage());
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
                .hora(mensaje.getHora().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                .borradoParaTodos(mensaje.getBorradoTodos())
                .tipoMensaje(mensaje.getTipo().name())
                .remitente(remitenteDTO)
                .multimedia(multimediaDTOs)
                .build();
    }
}
