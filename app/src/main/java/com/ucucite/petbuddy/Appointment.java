package com.ucucite.petbuddy;

import java.util.Random;
import java.util.UUID;

public class Appointment {
    private String serviceType;

    public enum Status { PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED }
    public enum PaymentStatus { PAID, UNPAID, PENDING }

    // Use String for unique ID (UUID-based)
    private String id;
    private String petName, serviceName, dateTime, total;
    private Status status;
    private PaymentStatus paymentStatus;
    private long createdTimeMillis;
    private long pendingDurationMillis;

    private boolean notifiedConfirmed;
    private boolean notifiedInProgress;
    private boolean notifiedCompleted;
    private boolean notifiedReminder;

    private Status lastKnownStatus;

    public Appointment() {
        assignIdIfMissing();
    }

    public Appointment(String id, String petName, String serviceType, String serviceName, String dateTime, String total,
                       PaymentStatus paymentStatus, Status status, long createdTimeMillis, long pendingDurationMillis,
                       boolean notifiedConfirmed, boolean notifiedInProgress, boolean notifiedCompleted, boolean notifiedReminder) {
        this.id = (id == null || id.trim().isEmpty()) ? generateUniqueId() : id;
        this.petName = petName;
        this.serviceType = serviceType;
        this.serviceName = serviceName;
        this.dateTime = dateTime;
        this.total = total;
        this.paymentStatus = paymentStatus;
        this.status = status;
        this.createdTimeMillis = createdTimeMillis;
        this.pendingDurationMillis = pendingDurationMillis;
        this.notifiedConfirmed = notifiedConfirmed;
        this.notifiedInProgress = notifiedInProgress;
        this.notifiedCompleted = notifiedCompleted;
        this.notifiedReminder = notifiedReminder;
        this.lastKnownStatus = status;
    }

    public Appointment(String petName, String serviceType, String serviceName, String dateTime, String total,
                       PaymentStatus paymentStatus, Status status, long createdTimeMillis) {
        this(null, petName, serviceType, serviceName, dateTime, total, paymentStatus, status, createdTimeMillis, generateRandomPendingDuration(),
                false, false, false, false);
    }

    public Appointment(String petName, String serviceType, String serviceName, String dateTime, String total,
                       PaymentStatus paymentStatus, Status status) {
        this(null, petName, serviceType, serviceName, dateTime, total, paymentStatus, status, System.currentTimeMillis(), generateRandomPendingDuration(),
                false, false, false, false);
    }

    private static long generateRandomPendingDuration() {
        Random random = new Random();
        return 10_000 + random.nextInt(51_000); // 10s to 60s
    }

    private static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    private void assignIdIfMissing() {
        if (this.id == null || this.id.trim().isEmpty()) {
            this.id = generateUniqueId();
        }
    }

    // --- Getters and setters ---
    public String getId() {
        assignIdIfMissing();
        return id;
    }
    public void setId(String id) {
        this.id = (id == null || id.trim().isEmpty()) ? generateUniqueId() : id;
    }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public String getTotal() { return total; }
    public void setTotal(String total) { this.total = total; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public long getCreatedTimeMillis() { return createdTimeMillis; }
    public void setCreatedTimeMillis(long createdTimeMillis) { this.createdTimeMillis = createdTimeMillis; }

    public long getPendingDurationMillis() { return pendingDurationMillis; }
    public void setPendingDurationMillis(long pendingDurationMillis) { this.pendingDurationMillis = pendingDurationMillis; }

    public boolean wasNotifiedConfirmed() { return notifiedConfirmed; }
    public void setWasNotifiedConfirmed(boolean v) { notifiedConfirmed = v; }

    public boolean wasNotifiedInProgress() { return notifiedInProgress; }
    public void setWasNotifiedInProgress(boolean v) { notifiedInProgress = v; }

    public boolean wasNotifiedCompleted() { return notifiedCompleted; }
    public void setWasNotifiedCompleted(boolean v) { notifiedCompleted = v; }

    public boolean wasNotifiedReminder() { return notifiedReminder; }
    public void setWasNotifiedReminder(boolean v) { notifiedReminder = v; }

    public Status getLastKnownStatus() { return lastKnownStatus == null ? status : lastKnownStatus; }
    public void setLastKnownStatus(Status s) { this.lastKnownStatus = s; }

    // Display for Care Type
    public String getCareTypeDisplay() {
        if (serviceType == null) return "";
        String value = serviceType.trim().toLowerCase();
        switch (value) {
            case "vet":
            case "veterinary":
            case "veterinarian":
                return "Veterinary";
            case "groom":
            case "grooming":
                return "Grooming";
            case "boarding":
                return "Boarding";
            default:
                return value.length() > 0 ? value.substring(0, 1).toUpperCase() + value.substring(1) : "";
        }
    }
}