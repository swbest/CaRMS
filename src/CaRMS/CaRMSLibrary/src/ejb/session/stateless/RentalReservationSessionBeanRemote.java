/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.RentalReservation;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import util.exception.CarCategoryNotFoundException;
import util.exception.InputDataValidationException;
import util.exception.ModelNotFoundException;
import util.exception.NoAvailableRentalRateException;
import util.exception.OutletNotFoundException;
import util.exception.RentalReservationNotFoundException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author dtjldamien
 */
public interface RentalReservationSessionBeanRemote {

    public Long createNewRentalReservation(RentalReservation newRentalReservation) throws InputDataValidationException, UnknownPersistenceException;

    public RentalReservation retrieveRentalReservationByRentalReservationId(Long rentalReservationId) throws RentalReservationNotFoundException;

    public List<RentalReservation> retrieveAllRentalReservations();

    public BigDecimal cancelReservation(Long rentalReservationId) throws RentalReservationNotFoundException;

    public void pickupCar(Long rentalReservationId) throws RentalReservationNotFoundException;

    public void returnCar(Long rentalReservationId) throws RentalReservationNotFoundException;

    public Boolean searchCarByModel(Date pickUpDateTime, Date returnDateTime, Long pickupOutletId, Long returnOutletId, Long modelId) throws NoAvailableRentalRateException, CarCategoryNotFoundException, OutletNotFoundException, ModelNotFoundException;

    public Boolean searchCarByCategory(Date pickUpDateTime, Date returnDateTime, Long pickupOutletId, Long returnOutletId, Long carCategoryId) throws NoAvailableRentalRateException, CarCategoryNotFoundException, OutletNotFoundException;

}
