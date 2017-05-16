package no.runsafe.creativetoolbox.command;

import no.runsafe.creativetoolbox.PlotFilter;
import no.runsafe.creativetoolbox.database.PlotTagRepository;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.OptionalArgument;
import no.runsafe.framework.api.command.player.PlayerAsyncCallbackCommand;
import no.runsafe.framework.api.player.IPlayer;

import java.util.List;
import java.util.Random;

public class RandomPlotCommand extends PlayerAsyncCallbackCommand<RandomPlotCommand.Sudo>
{
	public RandomPlotCommand(PlotFilter filter, IScheduler scheduler, PlotTagRepository tagRepository)
	{
		super("randomplot", "teleport to a random plot.", "runsafe.creative.teleport.random", scheduler, new OptionalArgument("tag"));
		plotFilter = filter;
		this.tagRepository = tagRepository;
		rng = new Random();
	}

	@Override
	public Sudo OnAsyncExecute(IPlayer executor, IArgumentList parameters)
	{
		if (plotFilter.getWorld() == null)
			return null;
		List<String> plots;
		if (parameters.getValue("tag") != null)
		{
			console.debugFine("Optional argument tag detected: %s", parameters.getValue("tag"));
			plots = tagRepository.findPlots(parameters.getValue("tag"));
			if (plots.isEmpty())
			{
				executor.sendColouredMessage("&cSorry, found no plots tagged \"%s\".", parameters.getValue("tag"));
				return null;
			}
		}
		else
			plots = plotFilter.getFiltered();
		int r = rng.nextInt(plots.size());
		Sudo target = new Sudo();
		target.player = executor;
		target.command = String.format("creativetoolbox teleport %s", plots.get(r));
		return target;
	}

	@Override
	public void SyncPostExecute(Sudo result)
	{
		if (result != null)
			result.player.performCommand(result.command);
	}

	class Sudo
	{
		public IPlayer player;
		public String command;
	}

	private final PlotTagRepository tagRepository;
	private final PlotFilter plotFilter;
	private final Random rng;
}
