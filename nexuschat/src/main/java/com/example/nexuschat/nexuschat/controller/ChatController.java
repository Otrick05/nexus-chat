package com.example.nexuschat.nexuschat.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.SortDefault;

import com.example.nexuschat.nexuschat.DTO.request.ActualizarChatRequestDTO;
import com.example.nexuschat.nexuschat.DTO.request.CreateChatRequestDTO;
import com.example.nexuschat.nexuschat.DTO.request.EnviarMensajeRequestDTO;
import com.example.nexuschat.nexuschat.DTO.response.ChatListResponseDTO;
import com.example.nexuschat.nexuschat.DTO.response.MensajeResponseDTO;
import com.example.nexuschat.nexuschat.DTO.response.PerfilUsuarioDTO;
import com.example.nexuschat.nexuschat.service.ChatGeneralService;
import com.example.nexuschat.nexuschat.model.Chat.TipoChat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/api/chats")
@Validated
@PreAuthorize("hasRole('ROLE_USUARIO')")
public class ChatController {

    private final ChatGeneralService chatGeneralService;

    public ChatController(ChatGeneralService chatGeneralService) {
        this.chatGeneralService = chatGeneralService;
    }

    @PostMapping("/newchat")
    public ResponseEntity<ChatListResponseDTO> crearchat(
            @Valid @RequestBody CreateChatRequestDTO request, Principal principal) {

        if (request.getTipo() != TipoChat.PRIVADO) {
            throw new IllegalArgumentException("El tipo de chat debe ser PRIVADO.");
        }
        ChatListResponseDTO chatCreado = chatGeneralService.crearChat(
                request, principal.getName());
        return new ResponseEntity<>(chatCreado, HttpStatus.CREATED);
    }

    @PostMapping("/grupo")
    public ResponseEntity<ChatListResponseDTO> crearGrupo(
            @Valid @RequestBody CreateChatRequestDTO request, Principal principal) {

        if (request.getTipo() != TipoChat.GRUPO) {
            throw new IllegalArgumentException("El tipo de chat debe ser GRUPO.");
        }

        ChatListResponseDTO chatCreado = chatGeneralService.crearGrupo(
                request, principal.getName());
        return new ResponseEntity<>(chatCreado, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ChatListResponseDTO>> getAllChats(Principal principal) {

        List<ChatListResponseDTO> chats = chatGeneralService
                .obtenerChatsDeUsuario(principal.getName());
        return new ResponseEntity<>(chats, HttpStatus.OK);
    }

    @PutMapping("(/grupo/{idChat}")
    public ResponseEntity<ChatListResponseDTO> updateChatDetails(
            @PathVariable Long idChat,
            @Valid @RequestBody ActualizarChatRequestDTO request,
            Principal principal) {

        ChatListResponseDTO chatActualizado = chatGeneralService.actualizarDetallesChat(
                idChat, request, principal.getName());
        return ResponseEntity.ok(chatActualizado);
    }

    @PostMapping("/grupo/{idChat}/participantes/{correo}")
    public ResponseEntity<Void> agregarParticipante(
            @PathVariable Long idChat,
            @PathVariable @NotEmpty @Email String correo,
            Principal principal) {

        chatGeneralService.agregarParticipante(idChat, principal.getName(),
                correo);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/grupo/{idChat}/participantes/{idUsuarioAExpulsar}")
    public ResponseEntity<Void> expulsarParticipante(
            @PathVariable Long idChat,
            @PathVariable Long idUsuarioAExpulsar,
            Principal principal) {

        chatGeneralService.expulsarParticipante(idChat, principal.getName(), idUsuarioAExpulsar);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/grupo/{idChat}/participantes/me/leave")
    public ResponseEntity<Void> salirDeChat(@PathVariable Long idChat, Principal principal) {
        chatGeneralService.salirDeChat(idChat, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/grupo/{idChat}/owner/{correo}")
    public ResponseEntity<Void> transferirPropiedad(
            @PathVariable Long idChat,
            @PathVariable @NotEmpty @Email String correo,
            Principal principal) {

        chatGeneralService.transferirPropiedad(idChat, principal.getName(), correo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{idChat}/mensajes")
    public ResponseEntity<MensajeResponseDTO> enviarMensaje(
            @PathVariable Long idChat,
            @Valid @RequestBody EnviarMensajeRequestDTO request,
            Principal principal) {

        request.setChatId(idChat);

        MensajeResponseDTO mensajeEnviado = chatGeneralService
                .procesarYDifundirMensaje(request, principal.getName());
        return new ResponseEntity<>(mensajeEnviado, HttpStatus.CREATED);
    }

    @GetMapping("/{idChat}/mensajes")
    public ResponseEntity<Page<MensajeResponseDTO>> obtenerMensajes(
            @PathVariable Long idChat,
            @SortDefault(sort = "hora", direction = Direction.DESC) Pageable pageable,
            Principal principal) {

        Page<MensajeResponseDTO> mensajes = chatGeneralService
                .obtenerMensajesDeChat(idChat, principal.getName(), pageable);
        return ResponseEntity.ok(mensajes);
    }

    @GetMapping("/grupo/{idChat}/participantes")
    public ResponseEntity<List<PerfilUsuarioDTO>> obtenerParticipantes(
            @PathVariable Long idChat,
            Principal principal) {

        List<PerfilUsuarioDTO> participantes = chatGeneralService.obtenerParticipantesChat(idChat, principal.getName());
        return ResponseEntity.ok(participantes);
    }

}