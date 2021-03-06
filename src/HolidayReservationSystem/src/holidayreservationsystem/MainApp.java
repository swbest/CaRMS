/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holidayreservationsystem;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import ws.client.CarCategory;
import ws.client.CarCategoryNotFoundException_Exception;
import ws.client.Customer;
import ws.client.CustomerNotFoundException_Exception;
import ws.client.InputDataValidationException_Exception;
import ws.client.InvalidLoginCredentialException;
import ws.client.InvalidLoginCredentialException_Exception;
import ws.client.Model;
import ws.client.ModelNotFoundException_Exception;
import ws.client.NoAvailableRentalRateException_Exception;
import ws.client.Outlet;
import ws.client.OutletNotFoundException_Exception;
import ws.client.Partner;
import ws.client.PartnerNameExistException_Exception;
import ws.client.PartnerNotFoundException_Exception;
import ws.client.RentalReservation;
import ws.client.RentalReservationNotFoundException_Exception;
import ws.client.UnknownPersistenceException_Exception;

/**
 *
 * @author sw_be
 */
class MainApp {

    private Long currentPartnerId = new Long(0);

    void runApp() {
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        while (true) {
            System.out.println("*** Welcome to Holiday Reservation System ***\n");
            System.out.println("1: Login");
            System.out.println("2: Search Car");
            System.out.println("3: Exit\n");
            response = 0;

            while (response < 1 || response > 3) {
                System.out.print("> ");

                response = scanner.nextInt();

                if (response == 1) {
                    try {
                        doPartnerLogin();
                        System.out.println("Login successful\n");
                        menuMain();
                    } catch (InvalidLoginCredentialException_Exception ex) {
                        System.out.println("Invalid login credential: " + ex.getMessage() + "\n");
                    }
                } else if (response == 2) {
                    doSearchCar();
                } else if (response == 3) {
                    break;
                } else {
                    System.out.print("Invalid option, please try again!\n");
                }
            }
            if (response == 3) {
                break;
            }
        }
    }

    private void doPartnerLogin() throws InvalidLoginCredentialException_Exception {
        Scanner scanner = new Scanner(System.in);
        String partnerName = "";
        String password = "";

        System.out.println("*** Holiday Reservation System :: Login ***\n");
        System.out.print("Enter partner name> ");
        partnerName = scanner.nextLine().trim();
        System.out.print("Enter password> ");
        password = scanner.nextLine().trim();

        if (partnerName.length() > 0 && password.length() > 0) {
            currentPartnerId = partnerLogin(partnerName, password);
        } else {
            InvalidLoginCredentialException ex = new InvalidLoginCredentialException();
            throw new InvalidLoginCredentialException_Exception("Missing login credential!", ex);
        }
    }

    private void doSearchCar() {
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Long carCategoryId = new Long(0); // to avoid error
        Long modelId = new Long(0); // to avoid error
        Date pickUpDateTime;
        Long pickupOutletId = new Long(0); // to avoid error
        Date returnDateTime;
        Long returnOutletId = new Long(0); // to avoid error
        XMLGregorianCalendar pickUpGregorianCalendar = null;
        XMLGregorianCalendar returnGregorianCalendar = null;
        GregorianCalendar gc = new GregorianCalendar();
        Boolean canReserve = false;

        System.out.println("*** Holiday Reservation System :: Search Car ***\n");

        try {
            System.out.print("Enter Pickup Date & Time (DD/MM/YYYY HH:MM)> ");
            pickUpDateTime = inputDateFormat.parse(scanner.nextLine().trim());
            gc.setTime(pickUpDateTime);
            pickUpGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
            System.out.print("Enter Return Date & Time (DD/MM/YYYY HH:MM)> ");
            returnDateTime = inputDateFormat.parse(scanner.nextLine().trim());
            gc.setTime(returnDateTime);
            returnGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
            List<Outlet> outlets = retrieveAllOutlets();
            if (returnDateTime.before(pickUpDateTime)) {
                throw new ReturnDateBeforePickupDateException();
            }

            System.out.printf("%4s%64s%20s%20s\n", "ID", "Outlet Name", "Opening Hour", "Closing Hour");
            SimpleDateFormat operatingHours = new SimpleDateFormat("HH:mm");
            for (Outlet outlet : outlets) {
                String openingHourString = "24/7";
                if (outlet.getOpeningHour() != null) {
                    XMLGregorianCalendar startGregorianCalendar = outlet.getOpeningHour();
                    Date openingHourDate = startGregorianCalendar.toGregorianCalendar().getTime();
                    openingHourString = operatingHours.format(openingHourDate);
                }
                String closingHourString = "";
                if (outlet.getClosingHour() != null) {
                    XMLGregorianCalendar endGregorianCalendar = outlet.getClosingHour();
                    Date closingHourDate = endGregorianCalendar.toGregorianCalendar().getTime();
                    closingHourString = operatingHours.format(closingHourDate);
                }
                System.out.printf("%4s%64s%20s%20s\n", outlet.getOutletId(), outlet.getOutletName(),
                        openingHourString, closingHourString);
            }

            System.out.print("Enter Pickup Outlet ID> ");
            pickupOutletId = scanner.nextLong();
            System.out.print("Enter Return Outlet ID> ");
            returnOutletId = scanner.nextLong();

            Outlet pickupOutlet = retrieveOutletByOutletId(pickupOutletId);
            if (pickupOutlet.getOpeningHour() != null) {
                Date pickupOutletOpeningHour = pickupOutlet.getOpeningHour().toGregorianCalendar().getTime();
                Date pickupOutletClosingHour = pickupOutlet.getClosingHour().toGregorianCalendar().getTime();
                if ((pickUpDateTime.getHours() < pickupOutletOpeningHour.getHours())
                        || (pickUpDateTime.getHours() == pickupOutletOpeningHour.getHours()
                        && pickUpDateTime.getMinutes() < pickupOutletOpeningHour.getMinutes())) {
                    throw new OutsideOperatingHoursException("Pickup time is before opening hours of the pickup outlet");
                } else if ((pickUpDateTime.getHours() > pickupOutletClosingHour.getHours())
                        || (pickUpDateTime.getHours() == pickupOutletClosingHour.getHours()
                        && pickUpDateTime.getMinutes() > pickupOutletClosingHour.getMinutes())) {
                    throw new OutsideOperatingHoursException("Pickup time is after closing hours of the pickup outlet");
                }
            }
            Outlet returnOutlet = retrieveOutletByOutletId(returnOutletId);
            if (pickupOutlet.getOpeningHour() != null) {
                Date returnOutletOpeningHour = returnOutlet.getOpeningHour().toGregorianCalendar().getTime();
                Date returnOutletClosingHour = returnOutlet.getClosingHour().toGregorianCalendar().getTime();
                if ((returnDateTime.getHours() < returnOutletOpeningHour.getHours())
                        || (returnDateTime.getHours() == returnOutletOpeningHour.getHours()
                        && returnDateTime.getMinutes() < returnOutletOpeningHour.getMinutes())) {
                    throw new OutsideOperatingHoursException("Return time is before opening hours of the return outlet");
                } else if ((returnDateTime.getHours() > returnOutletClosingHour.getHours())
                        || (returnDateTime.getHours() == returnOutletClosingHour.getHours()
                        && returnDateTime.getMinutes() > returnOutletClosingHour.getMinutes())) {
                    throw new OutsideOperatingHoursException("Return time is after closing hours of the return outlet");
                }
            }
            while (true) {
                System.out.println("*** Search by Car Category or Car Model? ***\n");
                System.out.println("1: Search by Car Category");
                System.out.println("2: Search by Car Model");
                response = 0;

                while (response < 1 || response > 2) {
                    System.out.print("> ");
                    response = scanner.nextInt();

                    if (response == 1) {
                        List<CarCategory> carCategories = retrieveAllCarCategories();
                        System.out.printf("%4s%64s\n", "ID", "Car Category Name");

                        for (CarCategory carCategory : carCategories) {
                            System.out.printf("%4s%64s\n",
                                    carCategory.getCarCategoryId(), carCategory.getCarCategoryName());
                        }
                        System.out.print("Enter Car Category ID> ");
                        carCategoryId = scanner.nextLong();
                        canReserve = searchCarByCategory(pickUpGregorianCalendar, returnGregorianCalendar, pickupOutletId, returnOutletId, carCategoryId);
                        break;
                    } else if (response == 2) {
                        List<Model> models = retrieveAllModels();
                        System.out.printf("%4s%64s%32s%32s\n", "ID", "Car Category Name", "Make", "Model");

                        for (Model model : models) {
                            System.out.printf("%4s%64s%32s%32s\n",
                                    model.getModelId(), model.getCarCategory().getCarCategoryName(),
                                    model.getMakeName(), model.getModelName());
                        }
                        System.out.print("Enter Car Model ID> ");
                        modelId = scanner.nextLong();
                        carCategoryId = retrieveModelByModelId(modelId).getCarCategory().getCarCategoryId();
                        canReserve = searchCarByModel(pickUpGregorianCalendar, returnGregorianCalendar, pickupOutletId, returnOutletId, modelId);
                        break;
                    }
                }
                if (response == 1 || response == 2) {
                    break;
                }
            }
            scanner.nextLine();
            if (!canReserve) {
                System.out.println("No cars are available under the provided criteria!");
            } else {
                BigDecimal totalRentalFee = calculateTotalRentalFee(carCategoryId, pickUpGregorianCalendar, returnGregorianCalendar);
                System.out.println("There are cars available! Total rental fee is SGD" + totalRentalFee + ". ");
                if (currentPartnerId > 0) {
                    System.out.print("Reserve a car? (Enter 'Y' to reserve a car)> ");
                    String input = scanner.nextLine().trim();
                    if (input.equals("Y")) {
                        doReserveCar(response, carCategoryId, modelId, pickUpDateTime, returnDateTime, pickupOutletId, returnOutletId, totalRentalFee);
                    }
                } else {
                    System.out.println("Please login to reserve the car!");
                }
            }
        } catch (ParseException ex) {
            System.out.println("Invalid date input!\n");
        } catch (CarCategoryNotFoundException_Exception ex) {
            System.out.println("Car Category not found for ID: " + carCategoryId + "\n");
        } catch (ModelNotFoundException_Exception ex) {
            System.out.println("Model not found!\n");
        } catch (OutletNotFoundException_Exception ex) {
            System.out.println("Outlet not found!\n");
        } catch (DatatypeConfigurationException ex) {
            System.out.println("DatatypeConfigurationException!\n");
        } catch (NoAvailableRentalRateException_Exception ex) {
            System.out.println("Outlet not found for ID: !" + pickupOutletId + " and/or " + returnOutletId + "\n");
        } catch (ReturnDateBeforePickupDateException ex) {
            System.out.println("Return Date is before Pickup Date!");
        } catch (OutsideOperatingHoursException ex) {
            System.out.println("Reservation is outside of outlet operating hours!");
        }
        System.out.print("Press any key to continue...> ");
        scanner.nextLine();
    }

    private void menuMain() {
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        while (true) {
            System.out.println("*** Holiday Reservation System ***\n");
            System.out.println("1: Search Car");
            System.out.println("2: View Reservation Details");
            System.out.println("3: View All Partner Reservations");
            System.out.println("4: Logout\n");
            response = 0;

            while (response < 1 || response > 4) {
                System.out.print("> ");

                response = scanner.nextInt();

                if (response == 1) {
                    doSearchCar();
                } else if (response == 2) {
                    doViewReservationDetails();
                } else if (response == 3) {
                    doViewAllReservations();
                } else if (response == 4) {
                    break;
                } else {
                    System.out.println("Invalid option, please try again!\n");
                }
            }
            if (response == 4) {
                break;
            }
        }
    }

    private void doReserveCar(Integer response, Long carCategoryId, Long modelId, Date pickUpDateTime, Date returnDateTime, Long pickupOutletId, Long returnOutletId, BigDecimal totalRentalFee) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** Holiday Reservation System :: Reserve Car ***\n");

        RentalReservation rentalReservation = new RentalReservation();
        Long customerId = new Long(0);
        String firstName;
        String lastName;
        String email;
        XMLGregorianCalendar pickUpGregorianCalendar = null;
        XMLGregorianCalendar returnGregorianCalendar = null;
        GregorianCalendar gc = new GregorianCalendar();

        try {
            gc.setTime(pickUpDateTime);
            pickUpGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
            rentalReservation.setStartDate(pickUpGregorianCalendar);
            gc.setTime(returnDateTime);
            returnGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
            rentalReservation.setEndDate(returnGregorianCalendar);
            rentalReservation.setPrice(totalRentalFee);
            Customer newCustomer = new Customer();

            System.out.print("Enter customer first name> ");
            firstName = scanner.nextLine().trim();
            System.out.print("Enter customer last name> ");
            lastName = scanner.nextLine().trim();
            System.out.print("Enter email> ");
            email = scanner.nextLine().trim();
            newCustomer.setFirstName(firstName);
            newCustomer.setLastName(lastName);
            newCustomer.setEmail(email);
            System.out.print("Enter credit card number> ");
            String creditCardNumber = scanner.nextLine().trim();
            rentalReservation.setCreditCardNumber(creditCardNumber);

            System.out.print("Would you like to pay now? (Enter 'Y' to enter payment details)> ");
            String input = scanner.nextLine().trim();
            if (input.equals("Y")) {
                rentalReservation.setPaid(Boolean.TRUE);
                System.out.println("Charged " + totalRentalFee.toString() + " to credit card: " + creditCardNumber);
            } else {
                rentalReservation.setPaid(Boolean.FALSE);
            }
            customerId = createNewCustomer(currentPartnerId, newCustomer);
            Long rentalReservationId = createNewPartnerRentalReservation(carCategoryId, currentPartnerId, modelId, customerId, pickupOutletId, returnOutletId, rentalReservation);
            System.out.println("Rental reservation created with ID: " + rentalReservationId);

        } catch (CarCategoryNotFoundException_Exception ex) {
            System.out.println("Car Category not found for ID: " + carCategoryId + "\n");
        } catch (ModelNotFoundException_Exception ex) {
            System.out.println("Model not found!\n");
        } catch (OutletNotFoundException_Exception ex) {
            System.out.println("Outlet not found!\n");
        } catch (PartnerNotFoundException_Exception ex) {
            System.out.println("Partner not found!\n");
        } catch (DatatypeConfigurationException | InputDataValidationException_Exception | UnknownPersistenceException_Exception | CustomerNotFoundException_Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void doCancelReservation(Long rentalReservationId) {
        Scanner scanner = new Scanner(System.in);
        RentalReservation rentalReservation;

        System.out.println("*** Holiday Reservation System :: Cancel Reservation ***\n");

        try {
            BigDecimal penalty = cancelReservation(rentalReservationId);
            rentalReservation = retrieveRentalReservationByRentalReservationId(rentalReservationId);

            System.out.println("Reservation successfully cancelled!");

            if (rentalReservation.isPaid()) {
                System.out.println("You have been refunded SGD $"
                        + rentalReservation.getPrice().subtract(penalty) + " to your card " 
                        + rentalReservation.getCreditCardNumber() + 
                        " after deducting cancellation penalty of SGD" + penalty + ".");
            } else {
                System.out.println("Your card : " + rentalReservation.getCreditCardNumber() + " has been charged SGD $" + penalty + " as a cancellation penalty.");
            }

        } catch (RentalReservationNotFoundException_Exception ex) {
            System.out.println("Rental Reservation not found for ID " + rentalReservationId);
        }
        System.out.print("Press any key to continue...> ");
        scanner.nextLine();
    }

    private void doViewReservationDetails() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** CaRMS Reservation Client :: View Reservation Details ***\n");
        System.out.print("Enter Reservation ID> ");
        Long rentalReservationId = scanner.nextLong();
        scanner.nextLine();

        try {
            RentalReservation rentalReservation = retrieveRentalReservationByRentalReservationId(rentalReservationId);
            System.out.printf("%4s%20s%20s%20s%12s%12s\n",
                    "ID", "Start Date",
                    "End Date", "Rental Fee",
                    "Paid", "Cancelled");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            XMLGregorianCalendar startGregorianCalendar = rentalReservation.getStartDate();
            XMLGregorianCalendar endGregorianCalendar = rentalReservation.getEndDate();
            Date startDate = startGregorianCalendar.toGregorianCalendar().getTime();
            Date endDate = endGregorianCalendar.toGregorianCalendar().getTime();

            System.out.printf("%4s%20s%20s%20s%12s%12s\n",
                    rentalReservation.getRentalReservationId(), sdf.format(startDate),
                    sdf.format(endDate), rentalReservation.getPrice().toString(),
                    rentalReservation.isPaid().toString(), rentalReservation.isIsCancelled().toString());
            System.out.print("Would you like to cancel the reservation? (Enter 'Y' to enter cancel the reservation)> ");
            String input = scanner.nextLine().trim();
            if (input.equals("Y")) {
                doCancelReservation(rentalReservationId);
            } else {
                System.out.print("Press any key to continue...> ");
            }
        } catch (RentalReservationNotFoundException_Exception ex) {
            System.out.println("Rental Reservation not found for ID " + rentalReservationId);
        }
        scanner.nextLine();
    }

    private void doViewAllReservations() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** Holiday Reservation System :: View All Reservations ***\n");
        List<RentalReservation> rentalReservations = retrievePartnerRentalReservations(currentPartnerId);
        System.out.printf("%4s%20s%20s\n", "ID", "Start Date", "End Date");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (RentalReservation rentalReservation : rentalReservations) {

            XMLGregorianCalendar startGregorianCalendar = rentalReservation.getStartDate();
            XMLGregorianCalendar endGregorianCalendar = rentalReservation.getEndDate();
            Date startDate = startGregorianCalendar.toGregorianCalendar().getTime();
            Date endDate = endGregorianCalendar.toGregorianCalendar().getTime();

            System.out.printf("%4s%20s%20s\n", rentalReservation.getRentalReservationId(),
                    sdf.format(startDate),
                    sdf.format(endDate));
        }
        System.out.print("Press any key to continue...> ");
        scanner.nextLine();
    }

    private static Long createNewCustomer(java.lang.Long arg0, ws.client.Customer arg1) throws InputDataValidationException_Exception, UnknownPersistenceException_Exception, PartnerNotFoundException_Exception {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.createNewCustomer(arg0, arg1);
    }

    private static Long createNewPartnerRentalReservation(java.lang.Long arg0, java.lang.Long arg1, java.lang.Long arg2, java.lang.Long arg3, java.lang.Long arg4, java.lang.Long arg5, ws.client.RentalReservation arg6) throws OutletNotFoundException_Exception, InputDataValidationException_Exception, UnknownPersistenceException_Exception, CarCategoryNotFoundException_Exception, CustomerNotFoundException_Exception, PartnerNotFoundException_Exception, ModelNotFoundException_Exception {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.createNewPartnerRentalReservation(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
    }

    private static Long partnerLogin(java.lang.String arg0, java.lang.String arg1) throws InvalidLoginCredentialException_Exception {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.partnerLogin(arg0, arg1);
    }

    private static BigDecimal calculateTotalRentalFee(Long arg0, XMLGregorianCalendar arg1, XMLGregorianCalendar arg2) throws CarCategoryNotFoundException_Exception, NoAvailableRentalRateException_Exception {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.calculateTotalRentalFee(arg0, arg1, arg2);
    }

    private static BigDecimal cancelReservation(java.lang.Long arg0) throws RentalReservationNotFoundException_Exception {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.cancelReservation(arg0);
    }

    private static Long createNewPartner(ws.client.Partner arg0) throws PartnerNameExistException_Exception, UnknownPersistenceException_Exception, InputDataValidationException_Exception {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.createNewPartner(arg0);
    }

    private static Long createNewRentalReservation(java.lang.Long arg0, java.lang.Long arg1, java.lang.Long arg2, java.lang.Long arg3, java.lang.Long arg4, ws.client.RentalReservation arg5) throws CustomerNotFoundException_Exception, InputDataValidationException_Exception, OutletNotFoundException_Exception, ModelNotFoundException_Exception, UnknownPersistenceException_Exception, CarCategoryNotFoundException_Exception {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.createNewRentalReservation(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    private static CarCategory retrieveCarCategoryByCarCategoryId(java.lang.Long arg0) throws CarCategoryNotFoundException_Exception {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.retrieveCarCategoryByCarCategoryId(arg0);
    }

    private static Model retrieveModelByModelId(java.lang.Long arg0) throws ModelNotFoundException_Exception {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.retrieveModelByModelId(arg0);
    }

    private static Outlet retrieveOutletByOutletId(java.lang.Long arg0) throws OutletNotFoundException_Exception {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.retrieveOutletByOutletId(arg0);
    }

    private static Partner retrievePartnerByPartnerId(java.lang.Long arg0) throws PartnerNotFoundException_Exception {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.retrievePartnerByPartnerId(arg0);
    }

    private static RentalReservation retrieveRentalReservationByRentalReservationId(java.lang.Long arg0) throws RentalReservationNotFoundException_Exception {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.retrieveRentalReservationByRentalReservationId(arg0);
    }

    private static java.util.List<ws.client.RentalReservation> retrieveAllRentalReservations() {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.retrieveAllRentalReservations();
    }

    private static java.util.List<ws.client.RentalReservation> retrievePartnerRentalReservations(java.lang.Long arg0) {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.retrievePartnerRentalReservations(arg0);
    }

    private static Boolean searchCarByCategory(XMLGregorianCalendar arg0, XMLGregorianCalendar arg1, Long arg2, Long arg3, Long arg4) throws OutletNotFoundException_Exception, CarCategoryNotFoundException_Exception, NoAvailableRentalRateException_Exception {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.searchCarByCategory(arg0, arg1, arg2, arg3, arg4);
    }

    private static Boolean searchCarByModel(XMLGregorianCalendar arg0, XMLGregorianCalendar arg1, Long arg2, Long arg3, Long arg4) throws NoAvailableRentalRateException_Exception, CarCategoryNotFoundException_Exception, ModelNotFoundException_Exception, OutletNotFoundException_Exception {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.searchCarByModel(arg0, arg1, arg2, arg3, arg4);
    }

    private static java.util.List<ws.client.CarCategory> retrieveAllCarCategories() {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.retrieveAllCarCategories();
    }

    private static java.util.List<ws.client.Model> retrieveAllModels() {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.retrieveAllModels();
    }

    private static java.util.List<ws.client.Outlet> retrieveAllOutlets() {
        ws.client.PartnerReservationWebService_Service service = new ws.client.PartnerReservationWebService_Service();
        ws.client.PartnerReservationWebService port = service.getPartnerReservationWebServicePort();
        return port.retrieveAllOutlets();
    }

    private static class OutsideOperatingHoursException extends Exception {

        public OutsideOperatingHoursException(String pickup_time_is_before_opening_hours_of_th) {
        }
    }

    private static class ReturnDateBeforePickupDateException extends Exception {

        public ReturnDateBeforePickupDateException() {
        }
    }
}
