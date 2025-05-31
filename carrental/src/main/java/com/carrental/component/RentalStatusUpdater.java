package com.carrental.component;

import com.carrental.entity.Car;
import com.carrental.entity.RentalRequest;
import com.carrental.repository.CarRepository;
import com.carrental.repository.RentalRequestRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.util.List;

@Component
public class RentalStatusUpdater {

    private final RentalRequestRepository rentalRequestRepository;
    private final CarRepository carRepository;

    @Autowired
    public RentalStatusUpdater(RentalRequestRepository rentalRequestRepository, CarRepository carRepository) {
        this.rentalRequestRepository = rentalRequestRepository;
        this.carRepository = carRepository;
    }

    @Scheduled(cron = "0 0 0 * * *") // Каждый день в полночь
    public void updateExpiredRentals() {
        LocalDate today = LocalDate.now();
        List<RentalRequest> allRequests = rentalRequestRepository.findAll();

        for (RentalRequest r : allRequests) {
            if ((r.getStatus().equals("pending") || r.getStatus().equals("approved")) && r.getEndDate().isBefore(today)) {
                r.setStatus("completed");
                rentalRequestRepository.save(r);

                Car rentedCar = r.getCar();
                rentedCar.setAvailable(true);
                carRepository.save(rentedCar);
            }
        }
    }
}
