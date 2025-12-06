package com.thahirsprojects.bookingservice.controller;

import com.thahirsprojects.bookingservice.request.BookingRequest;
import com.thahirsprojects.bookingservice.response.BookingResponse;
import com.thahirsprojects.bookingservice.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService){
        this.bookingService = bookingService;
    }

    @PostMapping(consumes = "application/json", produces = "application/json", path = "/booking" )
    public BookingResponse createBooking(@RequestBody BookingRequest request){
        return bookingService.createBooking(request);
    }
}
