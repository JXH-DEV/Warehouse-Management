package warehouse.dao;

import warehouse.model.Order;
import warehouse.model.Product;
import warehouse.model.Shipment;
import warehouse.model.User;

public final class DbEnumMapper {
    private DbEnumMapper() {}

    public static String toDb(User.Role role) {
        switch (role) {
            case ADMIN: return "ADMIN";
            case MANAGER: return "MENAXHER";
            case OPERATOR: return "OPERATOR";
            default: throw new IllegalArgumentException("Roli i panjohur: " + role);
        }
    }

    public static User.Role roleFromDb(String value) {
        switch (value) {
            case "ADMIN": return User.Role.ADMIN;
            case "MENAXHER": return User.Role.MANAGER;
            case "OPERATOR": return User.Role.OPERATOR;
            default: throw new IllegalArgumentException("Roli i panjohur në DB: " + value);
        }
    }

    public static String toDb(Order.Status status) {
        switch (status) {
            case PENDING: return "NE_PRITJE";
            case CONFIRMED: return "KONFIRMUAR";
            case PROCESSING: return "NE_PROCES";
            case SHIPPED: return "DERGUAAR";
            case DELIVERED: return "DOREZUAR";
            case CANCELLED: return "ANULUAR";
            default: throw new IllegalArgumentException("Statusi i panjohur: " + status);
        }
    }

    public static Order.Status orderStatusFromDb(String value) {
        switch (value) {
            case "NE_PRITJE": return Order.Status.PENDING;
            case "KONFIRMUAR": return Order.Status.CONFIRMED;
            case "NE_PROCES": return Order.Status.PROCESSING;
            case "DERGUAAR": return Order.Status.SHIPPED;
            case "DOREZUAR": return Order.Status.DELIVERED;
            case "ANULUAR": return Order.Status.CANCELLED;
            default: throw new IllegalArgumentException("Statusi i panjohur në DB: " + value);
        }
    }

    public static String toDb(Order.Type type) {
        switch (type) {
            case INCOMING: return "HYRJE";
            case OUTGOING: return "DALJE";
            default: throw new IllegalArgumentException("Lloji i panjohur: " + type);
        }
    }

    public static Order.Type orderTypeFromDb(String value) {
        switch (value) {
            case "HYRJE": return Order.Type.INCOMING;
            case "DALJE": return Order.Type.OUTGOING;
            default: throw new IllegalArgumentException("Lloji i panjohur në DB: " + value);
        }
    }

    public static String toDb(Shipment.Status status) {
        switch (status) {
            case PREPARING: return "NE_PERGATITJE";
            case DISPATCHED: return "DERGUAAR";
            case IN_TRANSIT: return "NE_TRANSIT";
            case DELIVERED: return "DOREZUAR";
            case RETURNED: return "KTHYER";
            default: throw new IllegalArgumentException("Statusi i panjohur: " + status);
        }
    }

    public static Shipment.Status shipmentStatusFromDb(String value) {
        switch (value) {
            case "NE_PERGATITJE": return Shipment.Status.PREPARING;
            case "DERGUAAR": return Shipment.Status.DISPATCHED;
            case "NE_TRANSIT": return Shipment.Status.IN_TRANSIT;
            case "DOREZUAR": return Shipment.Status.DELIVERED;
            case "KTHYER": return Shipment.Status.RETURNED;
            default: throw new IllegalArgumentException("Statusi i panjohur në DB: " + value);
        }
    }

    public static String toDb(Product.Category category) {
        return category.name();
    }

    public static Product.Category categoryFromDb(String value) {
        return Product.Category.valueOf(value);
    }
}
