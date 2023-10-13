package com.chel0per.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.chel0per.todolist.user.IUserRepository;
import com.chel0per.todolist.user.UserModel;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter{

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();

        if(servletPath.startsWith("/tasks/")){
            String authorization = request.getHeader("Authorization");

            String authEncoded = authorization.substring(5).trim();

            byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
            String authString = new String(authDecoded);
            String[] authValues  = authString.split(":");

            UserModel foundUser = this.userRepository.findByUsername(authValues[0]);

            if(foundUser == null){
                response.sendError(401);
            }
            else{
                Result passwordVerify = BCrypt.verifyer().verify(authValues[1].toCharArray(),foundUser.getPassword());
                if(passwordVerify.verified){
                    request.setAttribute("userId",foundUser.getId());
                    filterChain.doFilter(request, response);
                }
                else{
                    response.sendError(401);
                }
            }
        }
        else{
            filterChain.doFilter(request, response);
        }

    
        
    }
 
}
