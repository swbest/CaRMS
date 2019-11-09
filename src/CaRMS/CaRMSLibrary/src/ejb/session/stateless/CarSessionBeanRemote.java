/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.Car;
import java.util.List;
import util.exception.CarNotFoundException;
import util.exception.InputDataValidationException;
import util.exception.LicensePlateExistException;
import util.exception.ModelDisabledException;
import util.exception.ModelNotFoundException;
import util.exception.OutletNotFoundException;
import util.exception.UnknownPersistenceException;
import util.exception.UpdateCarException;

/**
 *
 * @author dtjldamien
 */
public interface CarSessionBeanRemote {

    public List<Car> retrieveAllCars();

    public Car retrieveCarByCarId(Long carId) throws CarNotFoundException;

    public void updateCar(Car car) throws CarNotFoundException, UpdateCarException, InputDataValidationException;

    public Long createNewCar(Long modelId, Long outletId, Car newCar) throws ModelDisabledException, ModelNotFoundException, OutletNotFoundException, LicensePlateExistException, UnknownPersistenceException, InputDataValidationException;

    public void deleteCar(Long carId) throws CarNotFoundException;

}
