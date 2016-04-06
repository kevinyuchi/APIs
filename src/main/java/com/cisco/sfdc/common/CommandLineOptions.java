package com.cisco.sfdc.common;

/**
 * Created by kevinyuchi on 1/19/16.
 */

public class CommandLineOptions {


    private String clientName;
    private Integer operationIndex;
    private String dateStart;
    private String dateEnd;
    private String configPath;
    private String prefix;
    private String dataSource;
    private String userId;
    private String scoreFileType;
    private String scoreFilePath;
    private String activityExportPath;
    private String leadExportPath;

    public String getScoreFileType() {
        return scoreFileType;
    }

    public void setScoreFileType(String scoreType) {
        this.scoreFileType = scoreType;
    }

    public String getScoreFilePath() {
        return scoreFilePath;
    }

    public void setScoreFilePath(String scoreFilePath) {
        this.scoreFilePath = scoreFilePath;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public Integer getOperationIndex() {
        return operationIndex;
    }

    public void setOperationIndex(Integer operationIndex) {
        this.operationIndex = operationIndex;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getDateStart() {
        return dateStart;
    }

    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getActivityExportPath() {
        return activityExportPath;
    }

    public void setActivityExportPath(String activityExportPath) {
        this.activityExportPath = activityExportPath;
    }

    public String getLeadExportPath() {
        return leadExportPath;
    }

    public void setLeadExportPath(String leadExportPath) {
        this.leadExportPath = leadExportPath;
    }
}
