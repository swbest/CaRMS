/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carmsmanagementclient;

import ejb.session.stateless.RentalReservationSessionBeanRemote;
import entity.Employee;
import entity.RentalReservation;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;
import util.enumeration.EmployeeRoleEnum;
import util.exception.InvalidAccessRightException;
import util.exception.RentalReservationNotFoundException;
import util.exception.UnpaidRentalReservationException;

/**
 *
 * @author dtjldamien
 */
public class CustomerServiceModule {

    private Employee currentEmployee;
    private RentalReservationSessionBeanRemote rentalReservationSessionBeanRemote;

    public CustomerServiceModule() {
    }

    public CustomerServiceModule(Employee currentEmployee, RentalReservationSessionBeanRemote rentalReservationSessionBeanRemote) {
        this();

        this.currentEmployee = currentEmployee;
        this.rentalReservationSessionBeanRemote = rentalReservationSessionBeanRemote;
    }

    public void menuCustomerService() throws InvalidAccessRightException, UnpaidRentalReservationException {

        if (currentEmployee.getEmployeeRole() != EmployeeRoleEnum.CUSTOMER_EXECUTIVE) {
            throw new InvalidAccessRightException("You don't have CUSTOMER_EXECUTIVE rights to access the customer service module.");
        }

        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        while (true) {
            System.out.println("*** CarMS Management Client :: Customer Service ***\n");
            System.out.println("1: Pickup Car");
            System.out.println("2: Return Car");
            System.out.println("3: Back\n");
            response = 0;

            while (response < 1 || response > 3) {
                System.out.print("> ");
                response = scanner.nextInt();
                if (response == 1) {
                    doPickupCar();
                } else if (response == 2) {
                    doReturnCar();
                } else if (response == 3) {
                    break;
                } else {
                    System.out.println("Invalid option, please try again!\n");
                }
            }
            if (response == 3) {
                break;
            }
        }
    }

    private void doPickupCar() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** CarMS Management Client :: Sales Management :: Pickup Car***\n");
        List<RentalReservation> rentalReservations = rentalReservationSessionBeanRemote.
                retrieveCustomerRentalReservationsByPickupOutletId(currentEmployee.getOutlet().getOutletId());
        if (rentalReservations.isEmpty()) {
            System.out.println("No cars to be picked up!");
        } else {
            try {
                System.out.printf("%4s%20s%20s\n", "ID", "Start Date", "End Date");
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                for (RentalReservation rentalReservation : rentalReservations) {
                    System.out.printf("%4s%20s%20s\n", rentalReservation.getRentalReservationId(),
                            sdf.format(rentalReservation.getStartDate()),
                            sdf.format(rentalReservation.getEndDate()));
                }
                System.out.print("Enter Rental Reservation ID> ");
                Long rentalReservationId = scanner.nextLong();
                scanner.nextLine();
                RentalReservation rentalReservation = rentalReservationSessionBeanRemote.retrieveRentalReservationByRentalReservationId(rentalReservationId);
                if (!rentalReservation.getPaid()) {
                    System.out.print("Pay rental fee? (Enter 'Y' to pay)> ");
                    String input = scanner.nextLine().trim();
                    if (!input.equals("Y")) {
                        throw new UnpaidRentalReservationException("Please pay for the rental reservation ID: " + rentalReservationId + " !");
                    } else {
                        System.out.println("Charged " + rentalReservation.getPrice().toString() + " to credit card: " + rentalReservation.getCreditCardNumber());
                    }
                }
                rentalReservationSessionBeanRemote.pickupCar(rentalReservationId);
                System.out.println("Car successfully picked up by customer");
            } catch (RentalReservationNotFoundException ex) {
                System.out.println(ex.getMessage());
            } catch (UnpaidRentalReservationException ex) {
                System.out.println(ex.getMessage());
            }
        }
        System.out.print("Press any key to continue...> ");
        scanner.nextLine();
    }

    private void doReturnCar() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** CarMS Management Client :: Sales Management :: Return Car***\n");
        List<RentalReservation> rentalReservations = rentalReservationSessionBeanRemote.
                retrieveCustomerRentalReservationsByReturnOutletId(currentEmployee.getOutlet().getOutletId());

        if (rentalReservations.isEmpty()) {
            System.out.println("No cars to be returned!");
        } else {
            System.out.printf("%4s%20s%20s\n", "ID", "Start Date", "End Date");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (RentalReservation rentalReservation : rentalReservations) {
                System.out.printf("%4s%20s%20s\n", rentalReservation.getRentalReservationId(),
                        sdf.format(rentalReservation.getStartDate()),
                        sdf.format(rentalReservation.getEndDate()));
            }
            System.out.print("Enter Rental Reservation ID> ");
            Long rentalReservationId = scanner.nextLong();
            scanner.nextLine();

            try {
                rentalReservationSessionBeanRemote.returnCar(rentalReservationId);
                System.out.println("Car returned by customer");
            } catch (RentalReservationNotFoundException ex) {
                System.out.println("No Rental Reservation of ID: " + rentalReservationId);
            }
            System.out.print("Press any key to continue...> ");
            scanner.nextLine();
        }
    }
}
