package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class TicketServiceImpl implements TicketService {

    private final int ADULT_FARE = 20;

    private final int CHILD_FARE = 10;

    private final TicketPaymentServiceImpl ticketPaymentService;

    private final SeatReservationServiceImpl seatReservationService;
    Predicate<Long> isValidAccountId = i -> (i != null && i > 0L);
    Predicate<List<TicketTypeRequest>> isAdultPresent = t -> t.parallelStream().anyMatch(request -> request.getTicketType().equals(TicketTypeRequest.Type.ADULT));
    Predicate<List<TicketTypeRequest>> isTicketCountPositive = t -> t.parallelStream().noneMatch(request -> request.getNoOfTickets() < 0);

    public TicketServiceImpl(final TicketPaymentServiceImpl ticketPaymentService, final SeatReservationServiceImpl seatReservationService) {
	this.ticketPaymentService = ticketPaymentService;
	this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
	final List<TicketTypeRequest> ticketTypeRequestList = validatePurchaseRequest(accountId, ticketTypeRequests);
	processBookingRequest(accountId, ticketTypeRequestList);
    }

    /*
    Validates accountId is not null and greater than 0.
    Adult present in ticketRequest.
    Number of Ticket count requested should not be negative. (Not present in Business rules)
    Should not exceed max count allowed to book.
     */
    private List<TicketTypeRequest> validatePurchaseRequest(final Long accountId, final TicketTypeRequest[] ticketTypeRequests) {
	if (!isValidAccountId.test(accountId) || ticketTypeRequests == null) {
	    throw new InvalidPurchaseException();
	}
	final List<TicketTypeRequest> ticketTypeRequestList = new ArrayList<>(Arrays.asList(ticketTypeRequests));
	ticketTypeRequestList.removeIf(Objects::isNull);
	if (!isAdultPresent.test(ticketTypeRequestList) || !isTicketCountPositive.test(ticketTypeRequestList) || getCountOfTickets(ticketTypeRequestList) > 20) {
	    throw new InvalidPurchaseException();
	}
	return ticketTypeRequestList;
    }

    private int getCountOfTickets(final List<TicketTypeRequest> ticketTypeRequestList) {
	return ticketTypeRequestList.stream().mapToInt(request -> request.getNoOfTickets()).sum();
    }

    /*
     Calculates count of tickets, amount to be paid for seat reservation and invoke payment and seat reservation service.
    */
    private void processBookingRequest(final Long accountId, final List<TicketTypeRequest> ticketTypeRequests) {
	int totalAmountToPay = getTotalAmountToPay(ticketTypeRequests);
	int totalSeatsToAllocate = getSeatCount(ticketTypeRequests);
	ticketPaymentService.makePayment(accountId, totalAmountToPay);
	seatReservationService.reserveSeat(accountId, totalSeatsToAllocate);
    }

    /*
    Exclude infant while counting number of tickets.
     */
    private int getSeatCount(final List<TicketTypeRequest> ticketTypeRequests) {
	return ticketTypeRequests.stream().filter(r -> !r.getTicketType().equals(TicketTypeRequest.Type.INFANT)).mapToInt(request -> request.getNoOfTickets()).sum();
    }

    private int getTotalAmountToPay(final List<TicketTypeRequest> ticketTypeRequests) {
	int adultTicketCount = 0;
	int childTicketCount = 0;
	for (TicketTypeRequest request : ticketTypeRequests) {
	    switch (request.getTicketType()) {
		case ADULT -> adultTicketCount += request.getNoOfTickets();
		case CHILD -> childTicketCount += request.getNoOfTickets();
	    }
	}
	return (adultTicketCount * ADULT_FARE) + (childTicketCount * CHILD_FARE);
    }


}
