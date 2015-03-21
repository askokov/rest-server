package uss.coi.cop.foundation.security.certs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import uss.coi.cop.foundation.models.Employee;
import uss.coi.cop.foundation.repositories.EmployeeRepository;
import uss.coi.cop.foundation.repositories.OnlineRepository;
import uss.coi.cop.foundation.utils.StringUtils;

//@Component
public class CertificateAuthenticationProvider implements AuthenticationProvider {


    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OnlineRepository onlineRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String login = authentication.getName();
        String password = authentication.getCredentials().toString();
        String certificate = (String) authentication.getDetails();
        System.out.println("certificate = " + certificate);
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
        return authentication.equals(CertificateAuthenticationProvider.class);
    }
}
