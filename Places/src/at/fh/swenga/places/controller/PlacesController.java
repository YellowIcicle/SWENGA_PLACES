package at.fh.swenga.places.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import at.fh.swenga.places.dao.CountryRepository;
import at.fh.swenga.places.dao.JourneyRepository;
import at.fh.swenga.places.dao.RecommendationRepository;
import at.fh.swenga.places.dao.UserCategoryDao;
import at.fh.swenga.places.dao.UserDao;
import at.fh.swenga.places.dao.UserRepository;
import at.fh.swenga.places.model.CountryModel;
import at.fh.swenga.places.model.JourneyModel;
import at.fh.swenga.places.model.RecommendationModel;
import at.fh.swenga.places.model.UserCategoryModel;
import at.fh.swenga.places.model.UserModel;

@Controller
//@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = "session")
public class PlacesController {

	@Autowired
	CountryRepository countryRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserCategoryDao userCatDao;

	@Autowired
	RecommendationRepository recommendationRepository;

	@Autowired
	UserDao userDao;

	@Autowired
	JourneyRepository journeyRepo;

	@Secured("ROLE_USER")
	@GetMapping("/achievements")
	@Transactional
	public String getAchievments(Model model) {

		return "achievements";

	}

	@Secured("ROLE_USER")
	@RequestMapping("/browse")
	@Transactional
	public String fillRecommendations(Model model) {

		List<RecommendationModel> allModels = recommendationRepository.findAll();
		RecommendationModel defaultModel = new RecommendationModel();

		if (allModels.size() > 0) {
			model.addAttribute("recommendations", allModels);
		} else {
			model.addAttribute("recommendations", defaultModel);
		}

		return "browse";
	}

	@Secured("ROLE_USER")
	@GetMapping("/contacts")
	@Transactional
	public String getContacts(Model model) {

		return "contacts";

	}

	@Secured("ROLE_USER")
	@GetMapping("/dashboard")
	@Transactional
	public String getDashboard(Model model, Authentication authentication) {

		UserModel user = userRepository.findFirstByUsername(authentication.getName());

		if (user != null && user.isEnabled()) {

			model.addAttribute("user", user);
		}
		return "dashboard";

	}

	@Secured("ROLE_USER")
	@PostMapping("/editUser")
	@Transactional
	public String editUser(@Valid UserModel changedUserModel, @RequestParam(value = "countryId") int cid,
			@RequestParam(value = "noShow") boolean no, BindingResult bindingResult, Model model,
			Authentication authentication) {

		List<CountryModel> countries = countryRepository.findAll();
		model.addAttribute("countries", countries);

		// Any errors? -> Create a String out of all errors and return to the page
		if (bindingResult.hasErrors()) {
			String errorMessage = "";
			for (FieldError fieldError : bindingResult.getFieldErrors()) {
				errorMessage += fieldError.getField() + " is invalid: " + fieldError.getCode() + "<br>";
			}
			model.addAttribute("errorMessage", errorMessage);
			return "forward:/userProfile";
		}

		UserModel user = userRepository.getOne(changedUserModel.getId());
		CountryModel country = countryRepository.getOne(cid);
		UserModel user1 = userRepository.findFirstByUsername(authentication.getName());

		if (user1 != null && user1.isEnabled()) {

			model.addAttribute("user", user1);
		}

		if (!changedUserModel.getUsername().equals(user.getUsername())) {
			if (userRepository.findFirstByUsername(changedUserModel.getUsername()) != null) {
				model.addAttribute("error", "Username is already in use, sorry!");
				return "dashboard";
			}
		}

		// Change the attributes
		user.setUsername(changedUserModel.getUsername());
		user.setFirstName(changedUserModel.getFirstName());
		user.setLastName(changedUserModel.getLastName());
		user.setMail(changedUserModel.getMail());
		user.setCountry(country);
		user.setPrivate(no);

		userRepository.save(user);

		return "redirect:/logout";

	}

	@Secured("ROLE_USER")
	@GetMapping("/following")
	@Transactional
	public String getFollowing(Model model) {

		return "following";

	}

	@GetMapping("/index")
	@Transactional
	public String getIndex(Model model) {

		return "index";

	}

	@Secured("ROLE_USER")
	@GetMapping("/journey")
	@Transactional
	public String getJourney(Model model) {

		List<CountryModel> countries = countryRepository.findAll();
		model.addAttribute("countries", countries);

		return "journey";

	}

	@Secured("ROLE_USER")
	@PostMapping("/addJourney")
	@Transactional
	public String addJourney(JourneyModel journey, Model model, @RequestParam(value = "countryId") int cid,
			@RequestParam(value = "countryId2") int cid2, @RequestParam(value = "departureDateS") String depD,
			@RequestParam(value = "arrivalDateS") String arD, Authentication auth) throws ParseException {

		Set<CountryModel> countries = new HashSet<CountryModel>();
		CountryModel country = countryRepository.getOne(cid);
		countries.add(country);
		if (cid2 != 999) {
			CountryModel country2 = countryRepository.getOne(cid2);
			countries.add(country2);
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date dateD = sdf.parse(depD);
		Calendar calD = Calendar.getInstance();
		calD.setTime(dateD);
		Date dateS = sdf.parse(arD);
		Calendar calS = Calendar.getInstance();
		calS.setTime(dateS);

		journey.setCountries(countries);
		journey.setDepartureDate(calS);
		journey.setArrivalDate(calD);

		UserModel user = userRepository.findByUsername(auth.getName());
		journey.setUsers(user);

		journeyRepo.save(journey);

		if (user != null && user.isEnabled()) {

			model.addAttribute("user", user);
		}

		return "dashboard";
	}

	@Secured("ROLE_USER")
	@RequestMapping(value = "/logout")
	public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		return "redirect:/login?logout";
	}

	@Secured("ROLE_USER")
	@GetMapping("/recommendations")
	@Transactional
	public String getRecommendations(Model model) {

		return "recommendations";

	}

	@GetMapping("/register")
	@Transactional
	public String getRegister(Model model) {

		List<CountryModel> countries = countryRepository.findAll();
		model.addAttribute("countries", countries);

		return "register";

	}

	@PostMapping("/register")
	@Transactional
	public String registerUser(@Valid UserModel user, BindingResult res, Model model, Authentication auth,
			@RequestParam(value = "countryId") int cid) {
		if (userRepository.findFirstByUsername(user.getUsername()) != null) {
			model.addAttribute("error", "Username is already in use, sorry!");
		} else {
			Set<UserCategoryModel> roles = new HashSet<UserCategoryModel>();
			UserCategoryModel catV = userCatDao.getRole("ROLE_VIEWER");
			UserCategoryModel catU = userCatDao.getRole("ROLE_USER");
			roles.add(catV);
			roles.add(catU);

			CountryModel country = countryRepository.getOne(cid);
			user.setPrivate(false);
			user.encryptPassword();
			user.setCategory(roles);
			user.setEnabled(true);
			user.setCountry(country);
			userDao.persist(user);

			model.addAttribute("message", "Welcome" + user.getUsername());
		}
		model.addAttribute("user", user);
		return "redirect:/login";
	}

	@Secured("ROLE_USER")
	@GetMapping("/userProfile")
	@Transactional
	public String getProfile(Model model, Authentication authentication) {

		List<CountryModel> countries = countryRepository.findAll();
		model.addAttribute("countries", countries);

		UserModel user = userRepository.findFirstByUsername(authentication.getName());

		if (user != null && user.isEnabled()) {

			model.addAttribute("user", user);
		}

		return "userProfile";

	}

	@RequestMapping("/visitor")
	@Transactional
	public String getVisitor(Model model) {

		UserModel user = userRepository.getDefaultUser("default");

		if (user != null && user.isEnabled()) {

			model.addAttribute("user", user);
		}

		return "index";

	}

	/*
	 * @Secured({ "ROLE_ADMIN" })
	 * 
	 * @GetMapping(value = "/changeUserRole") public String
	 * changeUserRole(@RequestParam("username") String username, Model model,
	 * Authentication authentication) {
	 * 
	 * UserModel user = userRepository.findFirstByUsername(username);
	 * 
	 * if (user != null && user.isEnabled()) {
	 * 
	 * if (user.getUsername().equalsIgnoreCase(authentication.getName())) {
	 * model.addAttribute("warningMessage",
	 * "You cannot change your own user role!"); return showUserManagement(model,
	 * authentication); }
	 * 
	 * model.addAttribute("user", user); return "changeUserRole"; } else {
	 * model.addAttribute("errorMessage",
	 * "Error while reading User data! Either dissabled or does not exist"); return
	 * showUserManagement(model, authentication); } }
	 */

	@Secured({ "ROLE_USER" })
	@PostMapping(value = "/changePassword")
	public String changePassword(@RequestParam(value = "oldPassword") String oldPassword, @RequestParam(value = "newPassword") String passwordNew, 
			@RequestParam(value = "usernameHidden") String username, Model model, Authentication authentication) {

		UserModel user = userRepository.findByUsername(authentication.getName());

		if(user != null && user.isEnabled()) {
			if ((user.getUsername().equalsIgnoreCase(authentication.getName())
					|| authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")))) {
				String passwAkt = user.getPassword();
				user.setPassword(oldPassword);
				user.encryptPassword();
				
				if(passwAkt == user.getPassword()) {
					userRepository.save(user);
					System.out.println("PW correcot");
					model.addAttribute("message", "Password successfully changed for User: " + username);					
					
					model.addAttribute("user", user);
					
					return"userProfile";
					
				}else {
					user.setPassword(passwAkt);
					user.encryptPassword();
					userRepository.save(user);
					System.out.println("PW not correct");
					model.addAttribute("warningMessage", "Error while reading User data!");					
									
					model.addAttribute("user", user);
					
					return "userProfile";
				}
			}
		}
		
		return"error";
	}

	@Secured({ "ROLE_USER" })
	@GetMapping(value = "/journeys")
	@Transactional
	public String showJourneys(Model model, Authentication auth) {

		UserModel current = userRepository.findByUsername(auth.getName());
		ArrayList<JourneyModel> journeys = journeyRepo.findByUsersId(current.getId());

		model.addAttribute("journeys", journeys);

		return "myJourneys";
	}
	
	@RequestMapping(value="maps")
	public String showMap() {
		return "maps";
	}

	@ExceptionHandler(Exception.class)
	public String handleAllException(Exception ex) {

		return "error";

	}

}