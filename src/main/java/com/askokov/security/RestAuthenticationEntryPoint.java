package uss.coi.cop.foundation.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import uss.coi.cop.foundation.form.AuthErrorDTO;
import uss.coi.cop.foundation.utils.JsonUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.Writer;

import static uss.coi.cop.foundation.utils.JsonUtils.convert2Json;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException ) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized" );
    }

    public static void write2Response(HttpServletResponse response, AuthErrorDTO authError) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response);
        Writer out = responseWrapper.getWriter();
        out.write(convert2Json(authError));
    }
}