
package services;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import utilities.AbstractTest;
import domain.Game;
import domain.Review;

/**
 * Esta clase permite la realizacion de los test correspondientes
 * a los casos de uso "A�adir cr�tica", "Editar cr�tica" y "Borrar cr�tica"
 * a los juegos para comprobar que se crean adecuadamente.
 * 
 * @author Jes�s V�zquez Argumedo
 * 
 */

@ContextConfiguration(locations = {
	"classpath:spring/junit.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class ReviewTest extends AbstractTest {

	// System under test ------------------------------------------------------
	@Autowired
	private ReviewService	reviewService;

	@Autowired
	private GameService		gameService;


	// Tests ------------------------------------------------------------------
	// FUNCTIONAL REQUIREMENTS
	// Un actor autenticado como cr�tico debe ser capaz de:
	// Administrar sus cr�ticas, lo que incluye crear, editar, listar y borrar.

	//Registrar una nueva cr�tica
	/**
	 * FUNCTIONAL REQUIREMENTS
	 * 
	 * En este test vamos a comprobar que un usuario puede a�adir correctamente
	 * una cr�tica a un juego siendo publicada solamente una por juego un mismo cr�tico.
	 * 
	 * El primer test negativo es causado porque ya tenemos una cr�tica publicada en ese juego por ese cr�tico,
	 * el segundo de ellos se produce porque estamos logeado por otro actor,
	 * el tercero se produce porque introducimos una valoraci�n fuera de rango del establecido y
	 * el cuarto es provocado porque el t�tulo lo hemos dejado en blanco.
	 * 
	 * @param No
	 *            es necesario parametro
	 * 
	 * 
	 */
	@Test
	public void driverRegisterReview() {
		final Object testingData[][] = {
			{
				"critic1", 96, "Titulo", "descripci�n", 0, true, null
			}, {
				"critic1", 96, "Titulo2", "descripci�n2", 10, false, null
			}, {
				"critic1", 94, "Titulo3", "descripci�n3", 5, false, IllegalArgumentException.class
			}, {
				"admin", 96, "Titulo4", "descripci�n4", 3, false, IllegalArgumentException.class
			}, {
				"critic2", 96, "Titulo5", "descripci�n5", 15, true, ConstraintViolationException.class
			}, {
				"critic3", 95, "", "descripci�n6", 7, true, ConstraintViolationException.class
			}
		};

		for (int i = 0; i < testingData.length; i++)
			this.registerReview((String) testingData[i][0], (int) testingData[i][1], (String) testingData[i][2], (String) testingData[i][3], (Integer) testingData[i][4], (Boolean) testingData[i][5], (Class<?>) testingData[i][6]);
	}
	protected void registerReview(final String loged, final int gameId, final String title, final String description, final Integer score, final Boolean draft, final Class<?> expected) {

		Game game = null;
		Review review = null;
		Class<?> caught = null;

		try {
			this.authenticate(loged);

			game = this.gameService.findOne(gameId);

			review = this.reviewService.create(game);

			review.setTitle(title);
			review.setDescription(description);
			review.setScore(score);
			review.setDraft(draft);

			review = this.reviewService.save(review);
			this.reviewService.findAll();
			this.reviewService.findOne(review.getId());

			this.unauthenticate();
		} catch (final Throwable oops) {
			caught = oops.getClass();
		}
		this.checkExceptions(expected, caught);
	}

	//Editar una cr�tica
	/**
	 * FUNCTIONAL REQUIREMENTS
	 * 
	 * En este test vamos a comprobar que un usuario puede editar correctamente
	 * una cr�tica a un juego siendo publicada solamente una por juego un mismo cr�tico.
	 * 
	 * El primer test negativo es causado porque ya tenemos una cr�tica publicada en ese juego por ese cr�tico,
	 * el segundo de ellos se produce porque estamos logeado con un cr�tico que es el propietario de esa cr�tica,
	 * el tercero se produce porque introducimos una valoraci�n fuera de rango del establecido y
	 * el cuarto es provocado porque el t�tulo lo hemos dejado en blanco.
	 * 
	 * @param No
	 *            es necesario parametro
	 * 
	 * 
	 */
	@Test
	public void driverEditReview() {
		final Object testingData[][] = {
			{
				"critic1", 125, "Titulo", "descripci�n", 0, true, null
			}, {
				"critic2", 129, "Titulo2", "descripci�n2", 10, true, null
			}, {
				"critic1", 125, "Titulo3", "descripci�n3", 5, false, IncorrectResultSizeDataAccessException.class
			}, {
				"critic3", 129, "Titulo4", "descripci�n4", 3, true, IllegalArgumentException.class
			}, {
				"critic2", 128, "Titulo5", "descripci�n5", 15, false, ConstraintViolationException.class
			}, {
				"critic1", 126, "", "descripci�n6", 7, false, ConstraintViolationException.class
			}
		};

		for (int i = 0; i < testingData.length; i++)
			this.editReview((String) testingData[i][0], (int) testingData[i][1], (String) testingData[i][2], (String) testingData[i][3], (Integer) testingData[i][4], (Boolean) testingData[i][5], (Class<?>) testingData[i][6]);
	}
	protected void editReview(final String loged, final int reviewId, final String title, final String description, final Integer score, final Boolean draft, final Class<?> expected) {

		Review review = null;
		Class<?> caught = null;

		try {
			this.authenticate(loged);

			review = this.reviewService.findOne(reviewId);

			review.setTitle(title);
			review.setDescription(description);
			review.setScore(score);
			review.setDraft(draft);

			review = this.reviewService.save(review);
			this.reviewService.findAll();

			this.unauthenticate();
		} catch (final Throwable oops) {
			caught = oops.getClass();
		}
		this.checkExceptions(expected, caught);
	}

	//Borrar una cr�tica
	/**
	 * FUNCTIONAL REQUIREMENTS
	 * 
	 * En este test vamos a comprobar que un usuario puede borrar correctamente
	 * una cr�tica a un juego.
	 * 
	 * El primer test negativo se produce porque estamos logeado con un cr�tico que es el propietario de esa cr�tica,
	 * el segundo de ellos se produce porque no se puede borrar una cr�tica ya publicada.
	 * 
	 * @param No
	 *            es necesario parametro
	 * 
	 * 
	 */
	@Test
	public void driverDeleteReview() {
		final Object testingData[][] = {
			{
				"critic1", 125, null
			}, {
				"critic2", 129, null
			}, {
				"critic3", 129, IllegalArgumentException.class
			}, {
				"critic1", 126, IllegalArgumentException.class
			}
		};

		for (int i = 0; i < testingData.length; i++)
			this.deleteReview((String) testingData[i][0], (int) testingData[i][1], (Class<?>) testingData[i][2]);
	}
	protected void deleteReview(final String loged, final int reviewId, final Class<?> expected) {

		Review review = null;
		Class<?> caught = null;

		try {
			this.authenticate(loged);

			review = this.reviewService.findOne(reviewId);

			this.reviewService.delete(review);
			this.reviewService.findAll();

			this.unauthenticate();
		} catch (final Throwable oops) {
			caught = oops.getClass();
		}
		this.checkExceptions(expected, caught);
	}
}
