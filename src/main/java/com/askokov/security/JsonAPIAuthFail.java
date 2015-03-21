package uss.coi.cop.foundation.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import uss.coi.cop.foundation.form.AuthErrorDTO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static uss.coi.cop.foundation.security.RestAuthenticationEntryPoint.write2Response;

public class JsonAPIAuthFail implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        write2Response(response, new AuthErrorDTO(false, exception.getMessage(), "critical", null));
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized" );
    }
}
