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

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

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
        
        // entityManager.getTransaction().begin();  //Cannot use an EntityTransaction while using JTA
        try (Connection conn = this.entityManager.unwrap(java.sql.Connection.class)) {
            logger.log(Level.INFO, "Connection established successfully: {0}", conn);
            
            try (PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            	statement.setString(1, coffee.getName());
            	statement.setFloat(2, coffee.getPrice().floatValue());
            	
            	int affectedRows = statement.executeUpdate();
                logger.log(Level.INFO, "Insert coffee successfully: {0}", affectedRows);
                
                try (ResultSet keys = statement.getGeneratedKeys()) {
                	keys.next();
                    coffee.setId(keys.getLong(1));
                	
                	// the following code proves the id from COFFEE table is the only result returned
                	/*
                    final ResultSetMetaData meta = keys.getMetaData();
                    final int columnCount = meta.getColumnCount();
                    final List<List<String>> rowList = new LinkedList<List<String>>();
                    while (keys.next())
                    {
                        final List<String> columnList = new LinkedList<String>();
                        rowList.add(columnList);

                        for (int column = 1; column <= columnCount; ++column) 
                        {
                            final Object value = keys.getObject(column);
                            columnList.add(String.valueOf(value));
                        }
                    }
                    logger.log(Level.INFO, "All data from result: {0}", rowList);
                    */
                	// logs
                	/*
					[2022/2/24  9:47:12:938 CST] 0000002e CafeRepositor I   Finding all coffees.
					[2022/2/24  9:47:12:939 CST] 0000002e sql           3   [eclipselink.sql] SELECT ID, NAME, PRICE FROM COFFEE
					[2022/2/24  9:47:13:580 CST] 00000037 CafeRepositor I   Persisting the new coffee cafe.model.entity.Coffee[id=null, name=AA, price=10.0].
					[2022/2/24  9:47:13:582 CST] 00000037 CafeRepositor I   Connection established successfully: com.ibm.ws.rsadapter.jdbc.v41.WSJdbc41Connection@6eeb0d3a
					[2022/2/24  9:47:13:880 CST] 00000037 CafeRepositor I   Insert coffee successfully: 1
					[2022/2/24  9:47:13:882 CST] 00000037 CafeRepositor I   All data from result: [[11]]
					[2022/2/24  9:47:14:212 CST] 00000028 CafeRepositor I   Finding all coffees.
					[2022/2/24  9:47:14:212 CST] 00000028 sql           3   [eclipselink.sql] SELECT ID, NAME, PRICE FROM COFFEE
                	 */
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
