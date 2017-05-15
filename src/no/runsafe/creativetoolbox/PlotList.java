package no.runsafe.creativetoolbox;

import com.google.common.collect.Lists;
import no.runsafe.framework.api.player.IPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlotList
{
	public void set(IPlayer player, List<String> list)
	{
		lists.put(player.getUniqueId(), list);
	}

	public int current(IPlayer player)
	{
		return lists.get(player.getUniqueId()).indexOf(pointer.get(player.getUniqueId())) + 1;
	}

	public void wind(IPlayer player, String to)
	{
		pointer.put(player.getUniqueId(), to);
	}

	public int count(IPlayer player)
	{
		return lists.get(player.getUniqueId()).size();
	}

	public void remove(String plot)
	{
		for (Map.Entry<UUID, List<String>> list : lists.entrySet())
		{
			if (list.getValue().contains(plot))
			{
				ArrayList<String> plots = Lists.newArrayList(list.getValue());
				int i = plots.indexOf(plot);
				plots.remove(plot);
				if (plots.isEmpty())
				{
					pointer.remove(list.getKey());
					lists.remove(list.getKey());
				}
				else
				{
					lists.put(list.getKey(), plots);
					if (plots.size() > i)
						pointer.put(list.getKey(), plots.get(i));
					else
						pointer.put(list.getKey(), plots.get(0));
				}
			}
		}
	}

	public String previous(IPlayer player)
	{
		if (lists.containsKey(player.getUniqueId()))
		{
			List<String> list = lists.get(player.getUniqueId());
			if (list == null || list.isEmpty())
				return null;
			int i = list.indexOf(pointer.get(player.getUniqueId()));
			pointer.put(player.getUniqueId(), list.get(i > 0 ? i - 1 : list.size() - 1));
			return pointer.get(player.getUniqueId());
		}
		return null;
	}

	public String next(IPlayer player)
	{
		if (lists.containsKey(player.getUniqueId()))
		{
			List<String> list = lists.get(player.getUniqueId());
			if (list == null || list.isEmpty())
				return null;
			int i = list.indexOf(pointer.get(player.getUniqueId()));
			pointer.put(player.getUniqueId(), list.get(i + 1 >= list.size() ? 0 : i + 1));
			return pointer.get(player.getUniqueId());
		}
		return null;
	}

	private final ConcurrentHashMap<UUID, String> pointer = new ConcurrentHashMap<UUID, String>();
	private final ConcurrentHashMap<UUID, List<String>> lists = new ConcurrentHashMap<UUID, List<String>>();
}
