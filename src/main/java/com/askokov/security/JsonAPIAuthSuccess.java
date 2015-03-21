package uss.coi.cop.foundation.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uss.coi.cop.foundation.form.AuthErrorDTO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static uss.coi.cop.foundation.security.RestAuthenticationEntryPoint.write2Response;

public class JsonAPIAuthSuccess extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private SessionManager sessionManager;

    private RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        write2Response(response, new AuthErrorDTO(true, null, null, this.sessionManager.getCurrentEmployee()));

        SavedRequest savedRequest = requestCache.getRequest(request, response);

        if (savedRequest == null) {
            clearAuthenticationAttributes(request);
            return;
        }
        String targetUrlParam = getTargetUrlParameter();
        if (isAlwaysUseDefaultTargetUrl() ||
                (targetUrlParam != null &&
                        StringUtils.hasText(request.getParameter(targetUrlParam)))) {
            requestCache.removeRequest(request, response);
            clearAuthenticationAttributes(request);
            return;
        }

        clearAuthenticationAttributes(request);
    }

}
