package com.planora.repository;

import com.planora.entity.Booking;
import com.planora.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByEvent_Id(Long eventId);

    boolean existsByEvent_IdAndVendor_IdAndStatusIn(Long eventId, Long vendorId, Collection<BookingStatus> statuses);
}
