package uk.gov.dwp.uc.pairtest;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceTest {

    @Mock
    private TicketPaymentServiceImpl ticketPaymentService;

    @Mock
    private SeatReservationServiceImpl seatReservationService;

    private TicketServiceImpl ticketService;

    @Before
    public void setup() {
	ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
    }


    @Test
    public void bookingWithNullParamsAdult() {
	ticketService.purchaseTickets(1L, null, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2));
	verify(ticketPaymentService).makePayment(1L, 40);
	verify(seatReservationService).reserveSeat(1L, 2);
    }

    @Test
    public void bookingShouldBeSuccessful() {
	ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2), new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2),
	    new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2));
	verify(ticketPaymentService).makePayment(1L, 60);
	verify(seatReservationService).reserveSeat(1L, 4);
    }

    @Test
    public void bookingTwentyTicketsShouldBeSuccessful() {
	ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 18), new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
	    new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1));
	verify(ticketPaymentService).makePayment(1L, 370);
	verify(seatReservationService).reserveSeat(1L, 19);
    }

    @Test
    public void adultChildBookingShouldBeSuccessful() {
	ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2), new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1));
	verify(ticketPaymentService).makePayment(1L, 50);
	verify(seatReservationService).reserveSeat(1L, 3);
    }

    @Test
    public void adultInfantBookingShouldBeSuccessful() {
	ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 3), new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1));
	verify(ticketPaymentService).makePayment(1L, 60);
	verify(seatReservationService).reserveSeat(1L, 3);
    }

    @Test
    public void adultOnlyBooking() {
	ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2));
	verify(ticketPaymentService).makePayment(1L, 40);
	verify(seatReservationService).reserveSeat(1L, 2);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void bookingWithNoParams() {
	ticketService.purchaseTickets(1L, null);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void bookingWithNullParamsChild() {
	ticketService.purchaseTickets(1L, null, new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2));
    }

    @Test(expected = InvalidPurchaseException.class)
    public void bookingWithNullParamsInfant() {
	ticketService.purchaseTickets(1L, null, new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2));
    }

    @Test(expected = InvalidPurchaseException.class)
    public void bookingWithNoParams3() {
	ticketService.purchaseTickets(1L, null, null, null);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void negativeCountBooking() {
	ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2), new TicketTypeRequest(TicketTypeRequest.Type.CHILD, -2),
	    new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2));
    }

    @Test(expected = InvalidPurchaseException.class)
    public void childOnlyBooking() {
	ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2));
    }

    @Test(expected = InvalidPurchaseException.class)
    public void infantOnlyBooking() {
	ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1));
    }

    @Test(expected = InvalidPurchaseException.class)
    public void bookingShouldBeFailureMaxTickets() {
	ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 18), new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
	    new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2));
    }

    @Test(expected = InvalidPurchaseException.class)
    public void shouldNotAllowBookingMoreThanMaxSeats() {
	ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 21));
    }


    @Test(expected = InvalidPurchaseException.class)
    public void accountIdValidationFailure() {
	ticketService.purchaseTickets(0L, new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1));
    }

    @Test(expected = InvalidPurchaseException.class)
    public void accountIdValidationForNullFailure() {
	ticketService.purchaseTickets(null, new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1));
    }

}
