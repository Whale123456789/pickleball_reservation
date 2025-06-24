package com.pickleball_backend.pickleball.service;

import com.pickleball_backend.pickleball.dto.AdminBookingDto;
import com.pickleball_backend.pickleball.dto.AdminUserDto;
import com.pickleball_backend.pickleball.entity.*;
import com.pickleball_backend.pickleball.repository.BookingRepository;
import com.pickleball_backend.pickleball.repository.CourtRepository;
import com.pickleball_backend.pickleball.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;

    @Override
    public List<AdminUserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToAdminUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public long getTotalUserCount() {
        return userRepository.count();
    }

    @Override
    public List<AdminBookingDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::convertToAdminBookingDto)
                .collect(Collectors.toList());
    }

    private AdminUserDto convertToAdminUserDto(User user) {
        AdminUserDto dto = new AdminUserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setDob(user.getDob());
        dto.setGender(user.getGender());
        dto.setUserType(user.getUserType());

        UserAccount account = user.getUserAccount();
        if (account != null) {
            dto.setUsername(account.getUsername());
            dto.setStatus(account.getStatus());
            dto.setProfileImage(account.getProfileImage());
        }

        Member member = user.getMember();
        if (member != null) {
            dto.setPointBalance(member.getPointBalance());
            if (member.getTier() != null) {
                dto.setTier(member.getTier().getTierName().name());
            }
        }
        return dto;
    }

    private AdminBookingDto convertToAdminBookingDto(Booking booking) {
        AdminBookingDto dto = new AdminBookingDto();
        dto.setId(booking.getId());
        dto.setBookingDate(booking.getBookingDate());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setStatus(booking.getStatus());

        if (booking.getMember() != null && booking.getMember().getUser() != null) {
            dto.setMemberName(booking.getMember().getUser().getName());
        }

        Slot slot = booking.getSlot();
        if (slot != null) {
            dto.setSlotDate(slot.getDate());
            dto.setStartTime(slot.getStartTime());
            dto.setEndTime(slot.getEndTime());

            Court court = courtRepository.findById(slot.getCourtId()).orElse(null);
            if (court != null) {
                dto.setCourtName(court.getName());
            }
        }

        dto.setPurpose(booking.getPurpose());
        dto.setNumberOfPlayers(booking.getNumberOfPlayers());
        return dto;
    }
}