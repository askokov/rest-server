package uss.coi.cop.foundation.security.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uss.coi.cop.foundation.models.helpers.Grants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uss.coi.cop.foundation.models.helpers.Grants.READ_DOCUMENT;

/**
 * This class has the rules for grant. It is loaded by reflection.
 * if you need to implement the new rule you should create the new  inner public static class.
 * Don't need to point in loader, it is loaded by reflection
 */
public final class GrantRulesImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrantRulesImpl.class);

    private GrantRulesImpl() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, GrantRule> getRules() {
        LOGGER.debug("Load rules");
        Map<String, GrantRule> rules = new ConcurrentHashMap<>();
        for (Class<GrantRule> grant : (Class<GrantRule>[]) GrantRulesImpl.class.getDeclaredClasses()) {
            try {
                insertRule(rules, grant.newInstance());
            } catch (Exception e) {
                LOGGER.error("Error occurred in loading grant : " + grant);
            }
        }
        return rules;
    }

    private static void insertRule(final Map<String, GrantRule> rules, GrantRule rule) {
        rules.put(rule.getGrant(), rule);
    }

    public static class ReadDocumentImpl extends GrantRule {
        public String getGrant() {
            return READ_DOCUMENT;
        }
    }

    public static class ReadEmployee extends GrantRule {
        public String getGrant() {
            return Grants.READ_EMPLOYEE;
        }
    }
    public static class CreateEmployee extends GrantRule {
        public String getGrant() {
            return Grants.CREATE_EMPLOYEE;
        }
    }
    public static class UpdateEmployee extends GrantRule {
        public String getGrant() {
            return Grants.UPDATE_EMPLOYEE;
        }
    }
    public static class DeleteEmployee extends GrantRule {
        public String getGrant() {
            return Grants.DELETE_EMPLOYEE;
        }
    }

    public static class ReadPerson extends GrantRule {
        public String getGrant() {
            return Grants.READ_PERSON;
        }
    }
    public static class CreatePerson extends GrantRule {
        public String getGrant() {
            return Grants.CREATE_PERSON;
        }
    }
    public static class UpdatePerson extends GrantRule {
        public String getGrant() {
            return Grants.UPDATE_PERSON;
        }
    }
    public static class DeletePerson extends GrantRule {
        public String getGrant() {
            return Grants.DELETE_PERSON;
        }
    }

    public static class ReadCrimeReportIml extends GrantRule {
        public String getGrant() {
            return Grants.READ_CRIME_REPORT;
        }
    }
    public static class CreateCrimeReportIml extends GrantRule {
        public String getGrant() {
            return Grants.CREATE_CRIME_REPORT;
        }
    }
    public static class UpdateCrimeReportIml extends GrantRule {
        public String getGrant() {
            return Grants.UPDATE_CRIME_REPORT;
        }
    }
    public static class DeleteCrimeReportIml extends GrantRule {
        public String getGrant() {
            return Grants.DELETE_CRIME_REPORT;
        }
    }

    public static class ReadDutyTeam extends GrantRule {
        public String getGrant() {
            return Grants.READ_DUTY_TEAM;
        }
    }
    public static class CreateDutyTeam extends GrantRule {
        public String getGrant() {
            return Grants.CREATE_DUTY_TEAM;
        }
    }
    public static class UpdateDutyTeam extends GrantRule {
        public String getGrant() {
            return Grants.UPDATE_DUTY_TEAM;
        }
    }
    public static class DeleteDutyTeam extends GrantRule {
        public String getGrant() {
            return Grants.DELETE_DUTY_TEAM;
        }
    }

    public static class ReadSceneProtocol extends GrantRule {
        public String getGrant() {
            return Grants.READ_SCENE_PROTOCOL;
        }
    }
    public static class CreateSceneProtocol extends GrantRule {
        public String getGrant() {
            return Grants.CREATE_SCENE_PROTOCOL;
        }
    }
    public static class UpdateSceneProtocol extends GrantRule {
        public String getGrant() {
            return Grants.UPDATE_SCENE_PROTOCOL;
        }
    }
    public static class DeleteSceneProtocol extends GrantRule {
        public String getGrant() {
            return Grants.DELETE_SCENE_PROTOCOL;
        }
    }

    public static class ReadCriminalCase extends GrantRule {
        public String getGrant() {
            return Grants.READ_CRIME_CASE;
        }
    }
    public static class CreateCriminalCase extends GrantRule {
        public String getGrant() {
            return Grants.CREATE_CRIME_CASE;
        }
    }
    public static class UpdateCriminalCase extends GrantRule {
        public String getGrant() {
            return Grants.UPDATE_CRIME_CASE;
        }
    }
    public static class DeleteCriminalCase extends GrantRule {
        public String getGrant() {
            return Grants.DELETE_CRIME_CASE;
        }
    }

    public static class ReadInquiryAction extends GrantRule {
        public String getGrant() {
            return Grants.READ_INQUIRY_ACTION;
        }
    }
    public static class CreateInquiryAction extends GrantRule {
        public String getGrant() {
            return Grants.CREATE_INQUIRY_ACTION;
        }
    }
    public static class UpdateInquiryAction extends GrantRule {
        public String getGrant() {
            return Grants.UPDATE_INQUIRY_ACTION;
        }
    }
    public static class DeleteInquiryAction extends GrantRule {
        public String getGrant() {
            return Grants.DELETE_INQUIRY_ACTION;
        }
    }

    public static class ReadPing extends GrantRule {
        public String getGrant() {
            return Grants.READ_PING;
        }
    }

    public static class ReadAttachment extends GrantRule {
        public String getGrant() {
            return Grants.READ_ATTACHMENT;
        }
    }
    public static class ReadMetadata extends GrantRule {
        public String getGrant() {
            return Grants.READ_META_DATA;
        }
    }

    public static class ReadAdvice extends GrantRule {
        public String getGrant() {
            return Grants.READ_ADVICE;
        }
    }
    public static class CreateAdvice extends GrantRule {
        public String getGrant() {
            return Grants.CREATE_ADVICE;
        }
    }
    public static class UpdateAdvice extends GrantRule {
        public String getGrant() {
            return Grants.UPDATE_ADVICE;
        }
    }
    public static class DeleteAdvice extends GrantRule {
        public String getGrant() {
            return Grants.DELETE_ADVICE;
        }
    }

    public static class ReadDuty extends GrantRule {
        public String getGrant() {
            return Grants.READ_DUTY;
        }
    }
    public static class CreateDuty extends GrantRule {
        public String getGrant() {
            return Grants.CREATE_DUTY;
        }
    }
    public static class UpdateDuty extends GrantRule {
        public String getGrant() {
            return Grants.UPDATE_DUTY;
        }
    }
    public static class DeleteDuty extends GrantRule {
        public String getGrant() {
            return Grants.DELETE_DUTY;
        }
    }
}
