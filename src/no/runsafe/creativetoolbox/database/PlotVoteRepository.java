package no.runsafe.creativetoolbox.database;

import no.runsafe.framework.api.database.ISchemaUpdate;
import no.runsafe.framework.api.database.Repository;
import no.runsafe.framework.api.database.SchemaUpdate;
import no.runsafe.framework.api.player.IPlayer;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

public class PlotVoteRepository extends Repository
{
	public boolean recordVote(IPlayer player, String plot)
	{
		return database.update(
			"INSERT INTO creative_plot_vote (`plot`, `player`, `rank`) VALUES (?, ?, ?)" +
				"ON DUPLICATE KEY UPDATE rank=VALUES(`rank`)",
			plot, player.getName(), StringUtils.join(player.getGroups(), ",")
		) > 0;
	}

	public void clear(String region)
	{
		database.execute("DELETE FROM creative_plot_vote WHERE `plot`=?", region);
	}

	public int tally(String regionName)
	{
		Integer answer = database.queryInteger("SELECT COUNT(*) AS tally FROM creative_plot_vote WHERE `plot`=?", regionName);
		if (answer == null)
			return 0;
		return answer;
	}

	public int tally(String region, Map<String, Integer> voteRanks)
	{
		List<String> votes = database.queryStrings("SELECT `rank` FROM creative_plot_vote WHERE `plot`=?", region);
		int tally = 0;
		for (String vote : votes)
			if (voteRanks.containsKey(vote.toLowerCase()))
				tally += voteRanks.get(vote.toLowerCase());
		return tally;
	}

	@Override
	public String getTableName()
	{
		return "creative_plot_vote";
	}

	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate update = new SchemaUpdate();

		update.addQueries(
			"CREATE TABLE creative_plot_vote (" +
				"`plot` varchar(255) NOT NULL," +
				"`player` varchar(255) NOT NULL," +
				"`rank` varchar(255) NOT NULL," +
				"PRIMARY KEY(`plot`,`player`)" +
			")"
		);

		return update;
	}
}