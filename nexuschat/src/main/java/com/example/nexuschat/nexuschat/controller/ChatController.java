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

import com.example.nexuschat.nexuschat.DTO.request.ActualizarChatRequestDTO;
import com.example.nexuschat.nexuschat.DTO.request.CreateChatRequestDTO;
import com.example.nexuschat.nexuschat.DTO.request.EnviarMensajeRequestDTO;
import com.example.nexuschat.nexuschat.DTO.response.ChatListResponseDTO;
import com.example.nexuschat.nexuschat.DTO.response.MensajeResponseDTO;
import com.example.nexuschat.nexuschat.service.ChatGeneralService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

@PreAuthorize("hasRole='USER'")
@RestController
@RequestMapping("/api/chat")
@Validated
public class ChatController {

    private final ChatGeneralService chatGeneralService;

    public ChatController(ChatGeneralService chatGeneralService) {
        this.chatGeneralService = chatGeneralService;
    }

    @PostMapping
    public ResponseEntity<ChatListResponseDTO> crearChat(
            @Valid @RequestBody CreateChatRequestDTO request, Principal principal) {

        ChatListResponseDTO chatCreado = chatGeneralService.crearChat(
                request, principal.getName());
        return new ResponseEntity<>(chatCreado, HttpStatus.ACCEPTED);
    }

    @GetMapping
    public ResponseEntity<List<ChatListResponseDTO>> getAllChats(Principal principal) {

        List<ChatListResponseDTO> chats = chatGeneralService
                .obtenerChatsDeUsuario(principal.getName());
        return new ResponseEntity<>(chats, HttpStatus.OK);
    }

    @PutMapping("/{idChat}")
    public ResponseEntity<ChatListResponseDTO> updateChatDetails(
            @PathVariable Long idChat,
            @Valid @RequestBody ActualizarChatRequestDTO request,
            Principal principal) {

        ChatListResponseDTO chatActualizado = chatGeneralService.actualizarDetallesChat(
                idChat, request, principal.getName());
        return ResponseEntity.ok(chatActualizado);
    }

    @PostMapping("/{idChat}/participantes/{correo}")
    public ResponseEntity<Void> agregarParticipante(
            @PathVariable Long idChat,
            @PathVariable @NotEmpty @Email String correo,
            Principal principal) {

        chatGeneralService.agregarParticipante(idChat, principal.getName(),
                correo);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{idChat}/participantes/{idUsuarioAExpulsar}")
    public ResponseEntity<Void> expulsarParticipante(
            @PathVariable Long idChat,
            @PathVariable Long idUsuarioAExpulsar,
            Principal principal) {

        chatGeneralService.expulsarParticipante(idChat, principal.getName(), idUsuarioAExpulsar);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{idChat}/participants/me/leave")
    public ResponseEntity<Void> salirDeChat(@PathVariable Long idChat, Principal principal) {
        chatGeneralService.salirDeChat(idChat, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{idChat}/owner/{correo}")
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
    public ResponseEntity<List<MensajeResponseDTO>> obtenerMensajes(
            @PathVariable Long idChat,
            Principal principal) {

        List<MensajeResponseDTO> mensajes = chatGeneralService
                .obtenerMensajesDeChat(idChat, principal.getName());
        return ResponseEntity.ok(mensajes);
    }

}