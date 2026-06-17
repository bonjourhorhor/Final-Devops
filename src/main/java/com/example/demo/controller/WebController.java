package com.example.demo.controller;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.repository.TemplateRepository;
import com.example.demo.service.ProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * Thymeleaf view controller – serves the web UI pages.
 */
@Controller
public class WebController {

    private final ProfileService profileService;
    private final TemplateRepository templateRepository;

    public WebController(ProfileService profileService, TemplateRepository templateRepository) {
        this.profileService = profileService;
        this.templateRepository = templateRepository;
    }

    /** Dashboard – list all profiles. */
    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) String type) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("profiles", profileService.searchProfiles(search));
            model.addAttribute("search", search);
        } else if (type != null && !type.isBlank()) {
            try {
                ProfileType pt = ProfileType.valueOf(type.toUpperCase());
                model.addAttribute("profiles", profileService.searchByType(pt));
                model.addAttribute("selectedType", type);
            } catch (IllegalArgumentException e) {
                model.addAttribute("profiles", profileService.getAllProfiles());
            }
        } else {
            model.addAttribute("profiles", profileService.getAllProfiles());
        }
        model.addAttribute("profileTypes", ProfileType.values());
        return "index";
    }

    /** Show create-profile form. */
    @GetMapping("/profiles/new")
    public String newProfileForm(Model model) {
        model.addAttribute("profile", new Profile());
        model.addAttribute("templates", templateRepository.findByActive(true));
        model.addAttribute("profileTypes", ProfileType.values());
        model.addAttribute("barcodeTypes", BarcodeType.values());
        model.addAttribute("isEdit", false);
        return "profile-form";
    }

    /** Handle create-profile form submission. */
    @PostMapping("/profiles/new")
    public String createProfile(
            @RequestParam String fullName,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String bloodGroup,
            @RequestParam ProfileType type,
            @RequestParam(required = false) MultipartFile photo,
            RedirectAttributes redirectAttributes) {

        try {
            Profile profile = Profile.builder()
                    .fullName(fullName)
                    .department(department)
                    .title(title)
                    .email(email)
                    .phone(phone)
                    .bloodGroup(bloodGroup)
                    .type(type)
                    .build();

            Profile created = profileService.createProfile(profile,
                    (photo != null && !photo.isEmpty()) ? photo : null);
            redirectAttributes.addFlashAttribute("success", "Profile created successfully! ID: " + created.getRegistrationNumber());
            return "redirect:/profiles/" + created.getId();
        } catch (IOException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create profile: " + e.getMessage());
            return "redirect:/profiles/new";
        }
    }

    /** Show profile detail page. */
    @GetMapping("/profiles/{id}")
    public String viewProfile(@PathVariable Long id, Model model) {
        Profile profile = profileService.getProfileById(id);
        model.addAttribute("profile", profile);
        return "profile-view";
    }

    /** Show edit-profile form. */
    @GetMapping("/profiles/{id}/edit")
    public String editProfileForm(@PathVariable Long id, Model model) {
        Profile profile = profileService.getProfileById(id);
        model.addAttribute("profile", profile);
        model.addAttribute("templates", templateRepository.findByActive(true));
        model.addAttribute("profileTypes", ProfileType.values());
        model.addAttribute("barcodeTypes", BarcodeType.values());
        model.addAttribute("isEdit", true);
        return "profile-form";
    }

    /** Handle edit-profile form submission. */
    @PostMapping("/profiles/{id}/edit")
    public String updateProfile(
            @PathVariable Long id,
            @RequestParam String fullName,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String bloodGroup,
            @RequestParam ProfileType type,
            @RequestParam(required = false) MultipartFile photo,
            RedirectAttributes redirectAttributes) {

        try {
            Profile updatedData = Profile.builder()
                    .fullName(fullName)
                    .department(department)
                    .title(title)
                    .email(email)
                    .phone(phone)
                    .bloodGroup(bloodGroup)
                    .type(type)
                    .build();

            MultipartFile photoFile = (photo != null && !photo.isEmpty()) ? photo : null;
            profileService.updateProfile(id, updatedData, photoFile);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/profiles/" + id;
        } catch (IOException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
            return "redirect:/profiles/" + id + "/edit";
        }
    }

    /** Delete profile and redirect to dashboard. */
    @PostMapping("/profiles/{id}/delete")
    public String deleteProfile(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        profileService.deleteProfile(id);
        redirectAttributes.addFlashAttribute("success", "Profile deleted successfully.");
        return "redirect:/";
    }
}
