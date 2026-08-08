package pe.edu.unmsm.fisi.gestiondocente.constancia.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IssuerPayload {

    private String system;

    @JsonProperty("executed_by_userid")
    private String executedByUserid;

    @JsonProperty("executed_by_email")
    private String executedByEmail;

    public IssuerPayload() {
    }

    public IssuerPayload(String system, String executedByUserid, String executedByEmail) {
        this.system = system;
        this.executedByUserid = executedByUserid;
        this.executedByEmail = executedByEmail;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getExecutedByUserid() {
        return executedByUserid;
    }

    public void setExecutedByUserid(String executedByUserid) {
        this.executedByUserid = executedByUserid;
    }

    public String getExecutedByEmail() {
        return executedByEmail;
    }

    public void setExecutedByEmail(String executedByEmail) {
        this.executedByEmail = executedByEmail;
    }
}
