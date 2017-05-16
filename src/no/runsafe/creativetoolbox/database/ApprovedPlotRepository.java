package no.runsafe.creativetoolbox.database;

import no.runsafe.framework.api.database.*;

import java.util.List;

public class ApprovedPlotRepository extends Repository
{
	public PlotApproval get(String plotName)
	{
		IRow data = database.queryRow("SELECT name, approved, approved_by FROM creativetoolbox_plot_approval WHERE name=?", plotName);
		if (data.isEmpty())
			return null;

		PlotApproval approval = new PlotApproval();
		approval.setName(data.String("name"));
		approval.setApproved(data.DateTime("approved"));
		approval.setApprovedBy(data.String("approved_by"));
		approval.setUniqueId("none");
		return approval;
	}

	public void persist(PlotApproval plotApproval)
	{
		database.update(
			"INSERT INTO creativetoolbox_plot_approval (name, approved, approved_by) VALUES (?, ?, ?)" +
				"ON DUPLICATE KEY UPDATE approved=VALUES(approved), approved_by=VALUES(approved_by)",
			plotApproval.getName(), plotApproval.getApproved(), plotApproval.getApprovedBy()
		);
	}

	public void delete(PlotApproval plotApproval)
	{
		database.execute("DELETE FROM creativetoolbox_plot_approval WHERE name=?", plotApproval.getName());
	}

	public List<String> getApprovedPlots()
	{
		return database.queryStrings("SELECT name FROM creativetoolbox_plot_approval");
	}

	@Override
	public String getTableName()
	{
		return "creativetoolbox_plot_approval";
	}

	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate update = new SchemaUpdate();

		update.addQueries(
			"CREATE TABLE creativetoolbox_plot_approval (" +
				"`name` varchar(255) NOT NULL," +
				"`approved` datetime NOT NULL," +
				"`approved_by` varchar(255) NOT NULL," +
				"PRIMARY KEY(`name`)" +
			")"
		);

		return update;
	}
}
