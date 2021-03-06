/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import util.enumeration.CarStatusEnum;

/**
 *
 * @author dtjldamien
 */
@Entity
public class Car implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long carId;
    @Column(nullable = false, length = 16, unique = true)
    @NotNull
    @Size(max = 16)
    private String licensePlate;
    @Column(nullable = false, length = 32)
    @NotNull
    @Size(max = 32)
    private String colour;
    @Column(nullable = false)
    @NotNull
    private CarStatusEnum carStatus; // true means on rental, false means in outlet
    @Column(nullable = false)
    @NotNull
    private Boolean isDisabled;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private Model model;
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(nullable = true)
    private Outlet outlet;
    @OneToOne(optional = true, fetch = FetchType.EAGER)
    private RentalReservation rentalReservation;

    public Car() {
        this.isDisabled = false;
        this.carStatus = CarStatusEnum.AVAILABLE;
    }

    public Car(String licensePlate, String colour) {
        this();

        this.licensePlate = licensePlate;
        this.colour = colour;
    }

    public Car(String licensePlate, String colour, CarStatusEnum carStatus) {
        this(licensePlate, colour);

        this.carStatus = carStatus;
    }

    public Long getCarId() {
        return carId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public CarStatusEnum getCarStatus() {
        return carStatus;
    }

    public void setCarStatus(CarStatusEnum carStatus) {
        this.carStatus = carStatus;
    }

    @XmlTransient
    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    @XmlTransient
    public Outlet getOutlet() {
        return outlet;
    }

    public void setOutlet(Outlet outlet) {
        this.outlet = outlet;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    @XmlTransient
    public RentalReservation getRentalReservation() {
        return rentalReservation;
    }

    public void setRentalReservation(RentalReservation rentalReservation) {
        this.rentalReservation = rentalReservation;
    }

    public Boolean getIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(Boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (carId != null ? carId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the carId fields are not set
        if (!(object instanceof Car)) {
            return false;
        }
        Car other = (Car) object;
        if ((this.carId == null && other.carId != null) || (this.carId != null && !this.carId.equals(other.carId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.Car[ id=" + carId + " ]";
    }
}
