
package controllers;

import java.util.Collection;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import services.ActorService;
import services.MessageEmailService;
import domain.Actor;
import domain.MessageEmail;

@Controller
@RequestMapping("/messageEmail")
public class MessageEmailController extends AbstractController {

	// Services ---------------------------------------------------------------

	@Autowired
	private MessageEmailService	messageEmailService;

	@Autowired
	private ActorService		actorService;


	// Constructors -----------------------------------------------------------

	public MessageEmailController() {
		super();
	}

	// List ----------------------------------------------------------------
	@RequestMapping(value = "/listOut", method = RequestMethod.GET)
	public ModelAndView listIn() {
		ModelAndView result;
		Collection<MessageEmail> messageEmails;
		final Actor actor = this.actorService.findByPrincipal();

		messageEmails = this.messageEmailService.findMessageEmailsSentByActorId(actor.getId());

		result = new ModelAndView("messageEmail/list");
		result.addObject("messageEmails", messageEmails);

		return result;
	}

	@RequestMapping(value = "/listIn", method = RequestMethod.GET)
	public ModelAndView listOut() {
		ModelAndView result;
		Collection<MessageEmail> messageEmails;
		final Actor actor = this.actorService.findByPrincipal();

		messageEmails = this.messageEmailService.findMessageEmailsReceivedByActorId(actor.getId());

		result = new ModelAndView("messageEmail/list");
		result.addObject("messageEmails", messageEmails);

		return result;
	}

	// Display ----------------------------------------------------------------
	@RequestMapping(value = "/display", method = RequestMethod.GET)
	public ModelAndView display(@RequestParam final int messageEmailId) {
		ModelAndView result;
		MessageEmail messageEmail;
		Boolean isRecipient = false;
		final Actor actor = this.actorService.findByPrincipal();

		messageEmail = this.messageEmailService.findOne(messageEmailId);

		/* Seguridad */
		if (!messageEmail.getRecipient().equals(actor) && !messageEmail.getSender().equals(actor))
			return result = new ModelAndView("redirect:../welcome/index.do");
		else if (messageEmail.getSender().equals(actor) && messageEmail.getDeletedForSender() == true)
			return result = new ModelAndView("redirect:../welcome/index.do");
		else if (messageEmail.getRecipient().equals(actor) && messageEmail.getDeletedForRecipient() == true)
			return result = new ModelAndView("redirect:../welcome/index.do");
		else {
			if (messageEmail.getRecipient().equals(actor))
				isRecipient = true;

			result = new ModelAndView("messageEmail/display");
			result.addObject("messageEmail", messageEmail);
			result.addObject("isRecipient", isRecipient);
		}
		return result;
	}

	@RequestMapping(value = "/create", method = RequestMethod.GET)
	public ModelAndView create(@RequestParam final int actorId) {
		ModelAndView result;
		final Actor recipient = this.actorService.findOne(actorId);

		final MessageEmail messageEmail = this.messageEmailService.create(recipient);
		result = this.createEditModelAndView(messageEmail);

		return result;
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST, params = "save")
	public ModelAndView saveResponse(@Valid final MessageEmail messageEmail, final BindingResult binding) {
		ModelAndView result;

		if (binding.hasErrors())
			result = this.createEditModelAndView(messageEmail);
		else
			try {
				this.messageEmailService.save(messageEmail);
				result = new ModelAndView("redirect:../messageEmail/listOut.do");
			} catch (final Throwable oops) {
				if (!this.messageEmailService.validatorURL(messageEmail.getAttachments()))
					result = this.createEditModelAndView(messageEmail, "messageEmail.commit.error.attachments");
				else
					result = this.createEditModelAndView(messageEmail, "messageEmail.commit.error");
			}

		return result;
	}

	//Reply ------------------------------------------------------------------------
	@RequestMapping(value = "/reply", method = RequestMethod.GET)
	public ModelAndView reply(@RequestParam final int messageEmailId) {
		ModelAndView result;
		final Actor actor = this.actorService.findByPrincipal();
		final MessageEmail messageEmailRequest = this.messageEmailService.findOne(messageEmailId);

		/* Seguridad */
		if (!messageEmailRequest.getRecipient().equals(actor) && !messageEmailRequest.getSender().equals(actor))
			return result = new ModelAndView("redirect:../../welcome/index.do");
		/*----*/
		else {
			final MessageEmail messageEmail = this.messageEmailService.reply(messageEmailRequest);
			result = this.createEditModelAndView(messageEmail);
		}

		return result;
	}

	//Forward ------------------------------------------------------------------------
	@RequestMapping(value = "/forward", method = RequestMethod.GET)
	public ModelAndView forward(@RequestParam final int messageEmailId) {
		ModelAndView result;
		final Actor actor = this.actorService.findByPrincipal();
		final MessageEmail messageEmailRequest = this.messageEmailService.findOne(messageEmailId);

		/* Seguridad */
		if (!messageEmailRequest.getRecipient().equals(actor) && !messageEmailRequest.getSender().equals(actor))
			return result = new ModelAndView("redirect:../welcome/index.do");
		/*----*/
		else {

			final MessageEmail messageEmail = this.messageEmailService.forward(messageEmailRequest);
			result = this.createEditModelAndViewForward(messageEmail);
		}

		return result;
	}

	@RequestMapping(value = "/forward", method = RequestMethod.POST, params = "save")
	public ModelAndView saveReply(@Valid final MessageEmail messageEmail, final BindingResult binding) {
		ModelAndView result;

		if (binding.hasErrors())
			result = this.createEditModelAndViewForward(messageEmail);
		else
			try {
				this.messageEmailService.save(messageEmail);
				result = new ModelAndView("redirect:../messageEmail/listOut.do");
			} catch (final Throwable oops) {
				if (!this.messageEmailService.validatorURL(messageEmail.getAttachments()))
					result = this.createEditModelAndView(messageEmail, "messageEmail.commit.error.attachments");
				else
					result = this.createEditModelAndView(messageEmail, "messageEmail.commit.error");
			}

		return result;
	}

	//Delete ------------------------------------------------------------------------
	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public ModelAndView delete(@Valid final int messageEmailId) {
		ModelAndView result;

		final MessageEmail messageEmail = this.messageEmailService.findOne(messageEmailId);
		final Actor actor = this.actorService.findByPrincipal();

		/* Seguridad */
		if (!messageEmail.getRecipient().equals(actor) && !messageEmail.getSender().equals(actor))
			return result = new ModelAndView("redirect:../welcome/index.do");

		else
			try {
				if (messageEmail.getRecipient().equals(actor)) {
					this.messageEmailService.delete(messageEmail);
					result = new ModelAndView("redirect:../messageEmail/listIn.do");
				} else {
					this.messageEmailService.delete(messageEmail);
					result = new ModelAndView("redirect:../messageEmail/listOut.do");
				}
			} catch (final Throwable oops) {
				result = new ModelAndView("redirect:../messageEmail/listIn.do");
			}
		return result;
	}

	//Ancillary methods---------------------------------------------------------

	//Create --------------------------------------------------------------------
	protected ModelAndView createEditModelAndView(final MessageEmail messageEmail) {
		final ModelAndView result = this.createEditModelAndView(messageEmail, null);
		return result;
	}

	protected ModelAndView createEditModelAndView(final MessageEmail messageEmail, final String message) {
		ModelAndView result;
		//Actor actor;
		final Collection<Actor> recipients;

		//actor = this.actorService.findByPrincipal();
		//recipients = this.actorService.findAll();
		//recipients.remove(actor);

		result = new ModelAndView("messageEmail/create");
		result.addObject("messageEmail", messageEmail);
		//result.addObject("recipients", recipients);
		result.addObject("message", message);
		return result;
	}

	//Forward ------------------------------------------------------------------------
	protected ModelAndView createEditModelAndViewForward(final MessageEmail messageEmail) {
		final ModelAndView result = this.createEditModelAndViewForward(messageEmail, null);
		return result;
	}

	protected ModelAndView createEditModelAndViewForward(final MessageEmail messageEmail, final String message) {
		ModelAndView result;
		Actor actor;
		Collection<Actor> recipients;

		actor = this.actorService.findByPrincipal();
		recipients = this.actorService.findAll();
		recipients.remove(actor);

		result = new ModelAndView("messageEmail/forward");
		result.addObject("messageEmail", messageEmail);
		result.addObject("recipients", recipients);
		result.addObject("message", message);
		return result;
	}

}
