package warehouse.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Shipment implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Status { PREPARING, DISPATCHED, IN_TRANSIT, DELIVERED, RETURNED }

    private int id;
    private String trackingNumber;
    private Order order;
    private LocalDate shipDate;
    private LocalDate estimatedArrival;
    private Status status;
    private String carrier;
    private String destination;
    private double weight;
    private String notes;

    public Shipment(int id, String trackingNumber, Order order, LocalDate shipDate,
                    LocalDate estimatedArrival, Status status, String carrier,
                    String destination, double weight, String notes) {
        this.id = id;
        this.trackingNumber = trackingNumber;
        this.order = order;
        this.shipDate = shipDate;
        this.estimatedArrival = estimatedArrival;
        this.status = status;
        this.carrier = carrier;
        this.destination = destination;
        this.weight = weight;
        this.notes = notes;
    }

    public int getId() { return id; }
    public String getTrackingNumber() { return trackingNumber; }
    public Order getOrder() { return order; }
    public LocalDate getShipDate() { return shipDate; }
    public LocalDate getEstimatedArrival() { return estimatedArrival; }
    public Status getStatus() { return status; }
    public String getCarrier() { return carrier; }
    public String getDestination() { return destination; }
    public double getWeight() { return weight; }
    public String getNotes() { return notes; }

    public void setStatus(Status status) { this.status = status; }
    public void setEstimatedArrival(LocalDate estimatedArrival) { this.estimatedArrival = estimatedArrival; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() { return trackingNumber; }
}
