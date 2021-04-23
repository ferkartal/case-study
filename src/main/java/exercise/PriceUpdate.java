package exercise;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PriceUpdate {

    private final String companyName;
    private final double price;

    public PriceUpdate(String companyName, double price) {
        this.companyName = companyName;
        this.price = price;
    }

    public String getCompanyName() {
        return this.companyName;
    }

    public double getPrice() {
        return this.price;
    }

    @Override
    public String toString() {
        return companyName + " - " + price;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        PriceUpdate that = (PriceUpdate) obj;

        return new EqualsBuilder()
                .append(companyName, that.companyName)
                .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(companyName)
                .toHashCode();
    }
}
