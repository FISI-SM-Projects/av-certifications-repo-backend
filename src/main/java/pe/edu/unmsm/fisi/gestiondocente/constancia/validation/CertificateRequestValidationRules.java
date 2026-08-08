package pe.edu.unmsm.fisi.gestiondocente.constancia.validation;

public final class CertificateRequestValidationRules {

    public static final int TEACHER_CODE_MIN = 1;
    public static final int TEACHER_CODE_MAX = 30;
    public static final int FULL_NAME_MIN = 2;
    public static final int FULL_NAME_MAX = 150;
    public static final int EMAIL_MAX = 254;
    public static final int COURSE_CODE_MIN = 1;
    public static final int COURSE_CODE_MAX = 30;
    public static final int SUBJECT_MIN = 1;
    public static final int SUBJECT_MAX = 200;
    public static final int CYCLE_MIN = 1;
    public static final int CYCLE_MAX = 10;
    public static final int SECTION_MIN = 1;
    public static final int SECTION_MAX = 20;
    public static final int SCHOOL_MIN = 1;
    public static final int SCHOOL_MAX = 30;
    public static final int PLAN_MIN = 1;
    public static final int PLAN_MAX = 30;
    public static final int SEMESTER_MIN = 1;
    public static final int SEMESTER_MAX = 20;
    public static final int ISSUER_SYSTEM_MIN = 1;
    public static final int ISSUER_SYSTEM_MAX = 50;
    public static final int EXECUTED_BY_USER_ID_MIN = 1;
    public static final int EXECUTED_BY_USER_ID_MAX = 100;
    public static final int EXPECTED_COURSES_MAX = 100;
    public static final int STORAGE_SEGMENT_MAX = 180;

    private CertificateRequestValidationRules() {
    }
}
