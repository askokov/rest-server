package uss.coi.cop.foundation.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import uss.coi.cop.foundation.exception.USSException;
import uss.coi.cop.foundation.repositories.OnlineRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("logoutHandler")
public class LogoutHandler extends SimpleUrlLogoutSuccessHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutHandler.class);

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private OnlineRepository onlineRepository;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            this.onlineRepository.setOffline(this.sessionManager.getLogin());
        } catch (USSException e) {
            LOGGER.error("Error occurred in logout", e);
            throw new ServletException(e);
        }
        super.onLogoutSuccess(request, response, authentication);
    }
}