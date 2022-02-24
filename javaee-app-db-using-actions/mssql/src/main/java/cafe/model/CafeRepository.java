package cafe.model;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import cafe.model.entity.Coffee;

@Stateless
public class CafeRepository {

	private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	@PersistenceContext
	private EntityManager entityManager;

	public List<Coffee> getAllCoffees() {
		logger.log(Level.INFO, "Finding all coffees.");

		return this.entityManager.createNamedQuery("findAllCoffees", Coffee.class).getResultList();
	}

	public Coffee persistCoffee(Coffee coffee) {
		logger.log(Level.INFO, "Persisting the new coffee {0}.", coffee);

//        this.entityManager.persist(coffee);

		String query = "INSERT INTO COFFEE (NAME, PRICE) VALUES (?, ?)";

		try (Connection conn = this.entityManager.unwrap(java.sql.Connection.class)) {
			logger.log(Level.INFO, "Connection established successfully: {0}", conn);

			try (PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
				statement.setString(1, coffee.getName());
				statement.setFloat(2, coffee.getPrice().floatValue());

				statement.executeUpdate();

				try (ResultSet keys = statement.getGeneratedKeys()) {
					keys.next();
					coffee.setId(keys.getLong(1));
				}
			}
		} catch (SQLException e) {
			logger.log(Level.INFO, "SQLException: {0}", e);
		}
		return coffee;
	}

	public void refreshCoffee(Coffee coffee) {
		Coffee managedCoffee = entityManager.merge(coffee);
		logger.log(Level.INFO, "Getting managed coffee: ", managedCoffee);
		this.entityManager.refresh(managedCoffee);
	}

	public void removeCoffeeById(Long coffeeId) {
		logger.log(Level.INFO, "Removing a coffee {0}.", coffeeId);

		Coffee coffee = entityManager.find(Coffee.class, coffeeId);
		this.entityManager.remove(coffee);
	}

	public Coffee findCoffeeById(Long coffeeId) {
		logger.log(Level.INFO, "Finding the coffee with id {0}.", coffeeId);

		return this.entityManager.find(Coffee.class, coffeeId);
	}
}
