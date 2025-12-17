package com.serverManagement.server.management.dto.rma;

public class DailyTrendDto {
    private String name;
    private long requests;

    public DailyTrendDto() {
    }

    public DailyTrendDto(String name, long requests) {
        this.name = name;
        this.requests = requests;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRequests() {
        return requests;
    }

    public void setRequests(long requests) {
        this.requests = requests;
    }
}
