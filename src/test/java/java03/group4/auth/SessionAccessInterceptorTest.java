package java03.group4.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

class SessionAccessInterceptorTest {

    private final SessionAccessInterceptor interceptor = new SessionAccessInterceptor();

    @Test
    void redirectsAnonymousUsersToLoginWithTheirRequestedPath() throws Exception {
        MockHttpServletRequest request = request("/catalog");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals("/login?redirect=%2Fcatalog", response.getRedirectedUrl());
    }

    @Test
    void studentsCanUseCatalogButNotRecords() throws Exception {
        MockHttpSession session = session("STUDENT");
        MockHttpServletRequest catalogRequest = request("/catalog/12");
        catalogRequest.setSession(session);
        assertTrue(interceptor.preHandle(catalogRequest, new MockHttpServletResponse(), new Object()));

        MockHttpServletRequest recordsRequest = request("/students");
        recordsRequest.setSession(session);
        MockHttpServletResponse denied = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(recordsRequest, denied, new Object()));
        assertEquals(403, denied.getStatus());
    }

    @Test
    void schoolStaffCanUseRecordsButNotStudentCatalog() throws Exception {
        MockHttpSession session = session("SCHOOL_STAFF");
        MockHttpServletRequest recordsRequest = request("/enrollments");
        recordsRequest.setSession(session);
        assertTrue(interceptor.preHandle(recordsRequest, new MockHttpServletResponse(), new Object()));

        MockHttpServletRequest catalogRequest = request("/catalog");
        catalogRequest.setSession(session);
        MockHttpServletResponse denied = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(catalogRequest, denied, new Object()));
        assertEquals(403, denied.getStatus());
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        return request;
    }

    private MockHttpSession session(String role) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", new User("test", "password", role));
        return session;
    }
}
