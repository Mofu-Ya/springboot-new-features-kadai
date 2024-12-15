package com.example.samuraitravel.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.repository.FavoriteRepository;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.FavoriteService;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {
	private final FavoriteRepository favoriteRepository;
	private final HouseRepository houseRepository;
	private final FavoriteService favoriteService;


	public FavoriteController(FavoriteRepository favoriteRepository, HouseRepository houseRepository, FavoriteService favoriteService) {
		this.favoriteRepository = favoriteRepository;
		this.houseRepository = houseRepository;
		this.favoriteService = favoriteService;
	}
	
	// お気に入り一覧を表示
	@GetMapping
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, Model model) {;
		User user = userDetailsImpl.getUser();
		Page<Favorite> favoritePage = favoriteRepository.findByUserOrderByCreatedAtDesc(user, pageable);
		
		model.addAttribute("favoritePage", favoritePage); 
		return "favorites/index";
	}
	
	@PostMapping("/{id}/toggle")
	public String toggle(@PathVariable(name = "id") Integer houseId, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes) {
		
		House house = houseRepository.getReferenceById(houseId);
		User user = userDetailsImpl.getUser();
		Optional<Favorite> favorite = favoriteRepository.findByUserAndHouseId(user, houseId);
		
		String message = "";
		
		if (favorite.isPresent()) {
	        favoriteRepository.delete(favorite.get());
	        message = "お気に入りを解除しました。";
	    } else {
	        favoriteService.create(house, user);
	        message = "お気に入りに追加しました。";
	    }
				
		redirectAttributes.addFlashAttribute("successMessage", message);    
        
        return "redirect:/houses/" + houseId;
	}
}