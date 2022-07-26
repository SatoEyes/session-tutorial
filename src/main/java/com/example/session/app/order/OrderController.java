package com.example.session.app.order;

import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.terasoluna.gfw.common.exception.BusinessException;
import org.terasoluna.gfw.common.message.ResultMessages;

import com.example.session.app.goods.GoodsSearchCriteria;
import com.example.session.domain.model.Cart;
import com.example.session.domain.model.Order;
import com.example.session.domain.service.order.EmptyCartOrderException;
import com.example.session.domain.service.order.InvalidCartOrderException;
import com.example.session.domain.service.order.OrderService;
import com.example.session.domain.service.userdetails.AccountDetails;

@Controller
@RequestMapping("order")
public class OrderController {

	@Inject
	OrderService orderService;
	
	@Inject
	Cart cart;
	
	@Inject
	GoodsSearchCriteria criteria;
	
	@GetMapping(params = "confirm")
	public String confirm(@AuthenticationPrincipal AccountDetails userDetails,
			Model model) {
		if(cart.isEmpty()) {
			ResultMessages messages = ResultMessages.error()
					.add("e.st.od.5001");
			model.addAttribute(messages);
			return "cart/viewCart";
		}
		model.addAttribute("account", userDetails.getAccount());
		model.addAttribute("signature", cart.calcSignature());
		return "order/confirm";
	}
	
	@PostMapping
	public String order(@AuthenticationPrincipal AccountDetails userDetails,
			@RequestParam String signature, RedirectAttributes attributes) {
		Order order = orderService.purchase(userDetails.getAccount(), cart, signature);
		attributes.addFlashAttribute(order);
		criteria.clear();
		return "redirect:/order?finish";
	}
	
	@GetMapping(params = "finish")
	public String finish() {
		return "order/finish";
	}
	
	@ExceptionHandler({EmptyCartOrderException.class, InvalidCartOrderException.class})
	@ResponseStatus(HttpStatus.CONFLICT)
	ModelAndView hadleOrderException(BusinessException e) {
		return new ModelAndView("common/error/businessError").addObject(e.getResultMessages());
	}	
}
