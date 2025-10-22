package com.jobplatform.web;

import com.jobplatform.domain.JobOffer;
import com.jobplatform.service.OfferService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Controller
public class OfferWebController {

    private final OfferService offerService;

    public OfferWebController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping("/offers")
    public String list(Model model) {
        model.addAttribute("offers", offerService.publicList());
        return "offers/list";
    }

    @GetMapping("/offers/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        JobOffer offer = offerService.get(id);
        model.addAttribute("offer", offer);
        return "offers/detail";
    }
}
