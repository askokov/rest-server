package uss.coi.cop.foundation.security.certs;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class CertificateAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private String certificate = null;

    public CertificateAuthenticationToken(Object principal, Object credentials, String certificate) {
        super(principal, credentials);
        this.certificate = certificate;
    }

    public final String getCertificate() {
        return certificate;
    }

    public final void setCertificate(String certificate) {
        this.certificate = certificate;
    }
}