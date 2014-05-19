import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;



/**
 * Main sample routine to show how to do basic operations with the package.
 * 
 * <p>
 * <b>NOTE:</b> We use asserts in a couple of places to verify the results but if this were actual production code, we
 * would have proper error handling.
 * </p>
 */
public class OrmLiteTest {

	// we are using the in-memory H2 database
	private final static String DATABASE_URL = "jdbc:sqlite:sample.db:account";
	
	private ConnectionSource connectionSource = null;


	private Dao<Account, Integer> accountDao;
	
	@Before
	public void setUp() throws Exception{
		connectionSource = new JdbcConnectionSource(DATABASE_URL);
		accountDao = DaoManager.createDao(connectionSource, Account.class);

        TableUtils.dropTable(connectionSource, Account.class, true);
		TableUtils.createTable(connectionSource, Account.class);
	}

	@After
	public void tearDown() throws Exception{
		if (connectionSource != null) {
			connectionSource.close();
		}
	}

	@Test
	public void readWriteData() throws Exception {
		// create an instance of Account
		String name = "Jim Coakley";
		Account account = new Account(name);

		// persist the account object to the database
		accountDao.create(account);
		int id = account.getId();
		verifyDb(id, account);

		// assign a password
		account.setPassword("_secret");
		// update the database after changing the object
		accountDao.update(account);
		verifyDb(id, account);

		// query for all items in the database
		List<Account> accounts = accountDao.queryForAll();
		Assert.assertEquals("Should have found 1 account matching our query", 1, accounts.size());
		verifyAccount(account, accounts.get(0));

		// loop through items in the database
		int accountC = 0;
		for (Account account2 : accountDao) {
			verifyAccount(account, account2);
			accountC++;
		}
		Assert.assertEquals("Should have found 1 account in for loop", 1, accountC);

		// construct a query using the QueryBuilder
		QueryBuilder<Account, Integer> statementBuilder = accountDao.queryBuilder();
		// shouldn't find anything: name LIKE 'hello" does not match our account
		statementBuilder.where().like(Account.NAME_FIELD_NAME, "hello");
		accounts = accountDao.query(statementBuilder.prepare());
		Assert.assertEquals("Should not have found any accounts matching our query", 0, accounts.size());

		// should find our account: name LIKE 'Jim%' should match our account
		statementBuilder.where().like(Account.NAME_FIELD_NAME, name.substring(0, 3) + "%");
		accounts = accountDao.query(statementBuilder.prepare());
		Assert.assertEquals("Should have found 1 account matching our query", 1, accounts.size());
		verifyAccount(account, accounts.get(0));

		// delete the account since we are done with it
		accountDao.delete(account);
		// we shouldn't find it now
		Assert.assertNull("account was deleted, shouldn't find any", accountDao.queryForId(id));
	}

	/**
	 * Example of reading and writing a large(r) number of objects.
	 */
	@Test
	public void readWriteBunch() throws Exception {

		Map<String, Account> accounts = new HashMap<String, Account>();
		for (int i = 1; i <= 100; i++) {
			String name = Integer.toString(i);
			Account account = new Account(name);
			// persist the account object to the database, it should return 1
			accountDao.create(account);
			accounts.put(name, account);
		}

		// query for all items in the database
		List<Account> all = accountDao.queryForAll();
		Assert.assertEquals("Should have found same number of accounts in map", accounts.size(), all.size());
		for (Account account : all) {
			Assert.assertTrue("Should have found account in map", accounts.containsValue(account));
			verifyAccount(accounts.get(account.getName()), account);
		}

		// loop through items in the database
		int accountC = 0;
		for (Account account : accountDao) {
			Assert.assertTrue("Should have found account in map", accounts.containsValue(account));
			verifyAccount(accounts.get(account.getName()), account);
			accountC++;
		}
		Assert.assertEquals("Should have found the right number of accounts in for loop", accounts.size(), accountC);
	}

	/**
	 * Example of created a query with a ? argument using the {@link com.j256.ormlite.stmt.SelectArg} object. You then can set the value of
	 * this object at a later time.
	 */
	@Test
	public void useSelectArgFeature() throws Exception {

		String name1 = "foo";
		String name2 = "bar";
		String name3 = "baz";
		Assert.assertEquals(1, accountDao.create(new Account(name1)));
		Assert.assertEquals(1, accountDao.create(new Account(name2)));
		Assert.assertEquals(1, accountDao.create(new Account(name3)));

		QueryBuilder<Account, Integer> statementBuilder = accountDao.queryBuilder();
		SelectArg selectArg = new SelectArg();
		// build a query with the WHERE clause set to 'name = ?'
		statementBuilder.where().like(Account.NAME_FIELD_NAME, selectArg);
		PreparedQuery<Account> preparedQuery = statementBuilder.prepare();

		// now we can set the select arg (?) and run the query
		selectArg.setValue(name1);
		List<Account> results = accountDao.query(preparedQuery);
		Assert.assertEquals("Should have found 1 account matching our query", 1, results.size());
		Assert.assertEquals(name1, results.get(0).getName());

		selectArg.setValue(name2);
		results = accountDao.query(preparedQuery);
		Assert.assertEquals("Should have found 1 account matching our query", 1, results.size());
		Assert.assertEquals(name2, results.get(0).getName());

		selectArg.setValue(name3);
		results = accountDao.query(preparedQuery);
		Assert.assertEquals("Should have found 1 account matching our query", 1, results.size());
		Assert.assertEquals(name3, results.get(0).getName());
	}
	
	@Test
	public void testTransactions() throws Exception{
		useTransactions(connectionSource);
	}

	/**
	 * Example of created a query with a ? argument using the {@link com.j256.ormlite.stmt.SelectArg} object. You then can set the value of
	 * this object at a later time.
	 */
	private void useTransactions(ConnectionSource connectionSource) throws Exception {
		String name = "trans1";
		final Account account = new Account(name);
		Assert.assertEquals(1, accountDao.create(account));

		TransactionManager transactionManager = new TransactionManager(connectionSource);
		try {
			// try something in a transaction
			transactionManager.callInTransaction(new Callable<Void>() {
				public Void call() throws Exception {
					// we do the delete
					Assert.assertEquals(1, accountDao.delete(account));
					Assert.assertNull(accountDao.queryForId(account.getId()));
					// but then (as an example) we throw an exception which rolls back the delete
					throw new Exception("We throw to roll back!!");
				}
			});
			Assert.fail("This should have thrown");
		} catch (SQLException e) {
			// expected
		}

		Assert.assertNotNull(accountDao.queryForId(account.getId()));
	}

	/**
	 * Verify that the account stored in the database was the same as the expected object.
	 */
	private void verifyDb(int id, Account expected) throws SQLException, Exception {
		// make sure we can read it back
		Account account2 = accountDao.queryForId(id);
		if (account2 == null) {
			throw new Exception("Should have found id '" + id + "' in the database");
		}
		verifyAccount(expected, account2);
	}

	/**
	 * Verify that the account is the same as expected.
	 */
	private static void verifyAccount(Account expected, Account account2) {
		Assert.assertEquals("expected name does not equal account name", expected, account2);
		Assert.assertEquals("expected password does not equal account name", expected.getPassword(), account2.getPassword());
	}
}