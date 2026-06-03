package id.perumdamts.mail.dto.id;

import id.perumdamts.mail.dto.id.marker.Employee;

public record EmployeeId(long value) implements SqidId {
    @Override
    public Class<?> getEntityClass() {
        return Employee.class;
    }
}
