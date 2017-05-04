package no.runsafe.creativetoolbox.database;

import no.runsafe.framework.api.database.*;
import no.runsafe.framework.api.player.IPlayer;

import java.util.List;

public class PlotLogRepository extends Repository
{
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean log(String plot, IPlayer claimer)
	{
		return database.execute("INSERT INTO `creative_plot_log` (`plot`,`claimer`,`claimed`) VALUES (?, ?, NOW())" +
			"ON DUPLICATE KEY UPDATE `claimer`=VALUES(`claimer`), `claimed`=VALUES(`claimed`)",
			plot, claimer.getName());
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean unknownPlayerLog(String plot)
	{
		return database.execute("INSERT INTO `creative_plot_log` (`plot`,`claimer`,`claimed`) VALUES (?, ?, NOW())" +
						"ON DUPLICATE KEY UPDATE `claimer`=VALUES(`claimer`), `claimed`=VALUES(`claimed`)",
				plot, "unknown");
	}

	public void delete(String plot)
	{
		database.execute("DELETE FROM `creative_plot_log` WHERE `plot`=?", plot);
	}

	public List<String> getPlots()
	{
		return database.queryStrings("SELECT `plot` FROM `creative_plot_log`");
	}

	public String getClaim(String plot)
	{
		IRow data = database.queryRow("SELECT * FROM creative_plot_log WHERE `plot`=?", plot);
		if (data.isEmpty())
			return null;
		return String.format("%s by %s", data.DateTime("claimed").toString("dd.MM.yyyy"), data.String("claimer"));
	}

	@Override
	public String getTableName()
	{
		return "creative_plot_log";
	}

	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate update = new SchemaUpdate();

		update.addQueries(
			"CREATE TABLE `creative_plot_log` (" +
				"`plot` varchar(255) not null," +
				"`claimer` varchar(255) not null," +
				"`claimed` datetime NOT NULL," +
				"PRIMARY KEY(`plot`)" +
			")"
		);

		return update;
	}

	public String getLatest(IPlayer player)
	{
		return database.queryString(
			"SELECT pl.`plot` " +
				"FROM `creative_plot_member` AS pm " +
				"JOIN `creative_plot_log` AS pl ON (pm.`plot` = pl.`plot` AND pm.`owner`=1) " +
				"WHERE pm.`player`=? " +
				"ORDER BY pl.`claimed` DESC " +
				"LIMIT 1",
			player.getName()
		);
	}
}
