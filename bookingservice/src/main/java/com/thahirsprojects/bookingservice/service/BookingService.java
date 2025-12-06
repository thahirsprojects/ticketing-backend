package com.thahirsprojects.bookingservice.service;

import com.thahirsprojects.bookingservice.client.InventoryServiceClient;
import com.thahirsprojects.bookingservice.entity.Customer;
import com.thahirsprojects.bookingservice.event.BookingEvent;
import com.thahirsprojects.bookingservice.repository.CustomerRepository;
import com.thahirsprojects.bookingservice.request.BookingRequest;
import com.thahirsprojects.bookingservice.response.BookingResponse;
import com.thahirsprojects.bookingservice.response.InventoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class BookingService {

    private final CustomerRepository customerRepository;
    private final InventoryServiceClient inventoryServiceClient;
    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

    @Autowired
    public BookingService(CustomerRepository customerRepository, InventoryServiceClient inventoryServiceClient, KafkaTemplate<String, BookingEvent>  kafkaTemplate) {
        this.customerRepository = customerRepository;
        this.inventoryServiceClient = inventoryServiceClient;
        this.kafkaTemplate = kafkaTemplate;
    }


    public BookingResponse createBooking(final BookingRequest request) {
        final Customer customer = customerRepository.findById(request.getUserId()).orElse(null);
        if(customer == null) {
            throw new RuntimeException("User not found");
        }
        final InventoryResponse inventoryResponse = inventoryServiceClient.getInventory(request.getEventId());
        log.info("Inventory response : {} ", inventoryResponse);
        if(inventoryResponse.getCapacity() < request.getTicketCount()){
            throw new RuntimeException("Not enough inventory");
        }
        //Booking
        final BookingEvent bookingEvent = createBooking(request, customer, inventoryResponse);
        // kafka template
        kafkaTemplate.send("booking", bookingEvent);
        log.info("Booking sent to Kafka: {}",bookingEvent);
        return BookingResponse.builder()
                .userId(bookingEvent.getUserId())
                .eventId(bookingEvent.getEventId())
                .ticketCount(bookingEvent.getTicketCount())
                .totalPrice(bookingEvent.getTotalPrice())
                .build();
    }

    private BookingEvent createBooking(BookingRequest request, Customer customer, InventoryResponse inventoryResponse) {
        return BookingEvent.builder()
                .userId(customer.getId())
                .eventId(request.getEventId())
                .ticketCount(request.getTicketCount())
                .totalPrice(inventoryResponse.getTicketPrice().multiply(BigDecimal.valueOf(request.getTicketCount())))
                .build();
    }
}
