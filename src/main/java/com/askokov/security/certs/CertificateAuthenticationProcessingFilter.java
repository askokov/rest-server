package uss.coi.cop.foundation.security.certs;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;

public class CertificateAuthenticationProcessingFilter extends UsernamePasswordAuthenticationFilter {
    private static final String CERTIFICATE_PARAM_KEY = "j_certificate";

    @Override
    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
        String certificate = request.getParameter(CERTIFICATE_PARAM_KEY);
        authRequest.setDetails(certificate);
    }
}