package uss.coi.cop.foundation.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ru.uss.esf.security.KeyStoreHelper;
import ru.uss.esf.security.validator.CertificateNotValid;
import ru.uss.esf.security.validator.CertificateRevoked;
import ru.uss.esf.security.validator.OCSPNotAvailableException;
import uss.coi.cop.foundation.encryption.CertificateValidatorService;
import uss.coi.cop.foundation.models.Employee;
import uss.coi.cop.foundation.repositories.EmployeeRepository;
import uss.coi.cop.foundation.repositories.OnlineRepository;
import uss.coi.cop.foundation.utils.StringUtils;

import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OnlineRepository onlineRepository;

    @Autowired
    private CertificateValidatorService certificateValidatorService;

    /**
     * This method compares the pair login/password with db data
     *
     * @param authentication the auth data
     * @return true if the pair compares
     */
    @Override
    public Authentication authenticate(Authentication authentication) {
        String login = authentication.getName();
        String password = authentication.getCredentials().toString();
        String certificate = (String) authentication.getDetails();
        validateCertificate(certificate);
        Employee employee = employeeRepository.findByLogin(login);
        if (employee != null && StringUtils.equalsPwd(employee.getPassword(), StringUtils.generatePwd(password))) {
            this.onlineRepository.isOnline(login);
            return new UsernamePasswordAuthenticationToken(login, password, null);
        } else {
            throw new BadCredentialsException("The pair login/password was not found!");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    void validateCertificate(String certificateString) {
        if (certificateString == null || certificateString.isEmpty()) {
            throw new BadCredentialsException("Certificate is null or empty");
        }
        try {
            X509Certificate certificate = KeyStoreHelper.loadFromString(certificateString);
            certificateValidatorService.checkCertificateOCSP(certificate);
        } catch (CertificateException e) {
            throw new BadCredentialsException("Certificate error");
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        } catch (CertificateNotValid certificateNotValid) {
            throw new BadCredentialsException("Certificate not valid");
        } catch (CertificateRevoked certificateRevoked) {
            throw new BadCredentialsException("Certificate revoked");
        } catch (OCSPNotAvailableException e) {
            throw new BadCredentialsException("OCSP Service not available");
        }
    }
}
