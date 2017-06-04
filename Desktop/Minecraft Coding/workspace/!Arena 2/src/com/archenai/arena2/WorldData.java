package com.archenai.arena2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class WorldData 
{
	private String name, authors;
	private List<CoordSet> startLocations;
	private CoordSet spectatorStartLocation;
	private Material collapseBlock;
	private int UID;
	private String fileName;
	private WorldType worldType;
	private CollapseType collapseType;
	
	public WorldData(String name, String authors, List<CoordSet> startLocations, Material collapseBlock, int UID, 
			String fileName, WorldType worldType, CollapseType collapseType)
	{
		this.name = name;
		this.authors = authors;
		this.startLocations = startLocations;
		this.collapseBlock = collapseBlock;
		this.UID = UID;
		this.fileName = fileName;
		this.worldType = worldType;
		this.collapseType = collapseType;
	}
	
	public WorldData(File f, boolean spawn)
	{
		if(f.exists())
		{
			try {
				startLocations = new ArrayList<CoordSet>();
				FileReader fileReader = new FileReader(f);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String s = "";
				while(spawn && (s = bufferedReader.readLine()) != null)
				{
					if(s.startsWith("name="))
						this.name = s.substring(5);
					else if(s.startsWith("authors="))
						this.authors = s.substring(8);
					else if(s.startsWith("startloc="))
					{
						int b = 0, c = 9;
						CoordSet startLoc = new CoordSet(0, 0, 0);
						for(int i = 9; i < s.length(); i++)
						{
							if(s.charAt(i) == ',' || i == s.length() - 1)
								switch(b)
								{
								case 0:
									startLoc.setX(Double.valueOf(s.substring(c, i)));
									c = i + 1;
									b++;
									break;
								case 1:
									startLoc.setY(Double.valueOf(s.substring(c, i)));
									c = i + 1;
									b++;
									break;
								case 2:
									startLoc.setZ(Double.valueOf(s.substring(c)));
									break;
								}
						}
						this.startLocations.add(startLoc); //s.substring(5);
					} else if(s.startsWith("spectatorstart="))
					{
						int b = 0, c = 15;
						CoordSet startLoc = new CoordSet(0, 0, 0);
						for(int i = 15; i < s.length(); i++)
						{
							if(s.charAt(i) == ',' || i == s.length() - 1)
								switch(b)
								{
								case 0:
									startLoc.setX(Double.valueOf(s.substring(c, i)));
									c = i + 1;
									b++;
									break;
								case 1:
									startLoc.setY(Double.valueOf(s.substring(c, i)));
									c = i + 1;
									b++;
									break;
								case 2:
									startLoc.setZ(Double.valueOf(s.substring(c)));
									break;
								}
						}
						spectatorStartLocation = startLoc;
					}
					else if(s.startsWith("filename="))
						this.fileName = s.substring(9);
					else if(s.startsWith("uid="))
						this.UID = Integer.valueOf(s.substring(4));
					else if(s.startsWith("worldtype="))
						this.worldType = WorldType.valueOf(s.substring(10).toUpperCase());
					this.collapseType = null;
					this.collapseBlock = null;
				}
				while(!spawn && (s = bufferedReader.readLine()) != null)
				{
					if(s.startsWith("name="))
						this.name = s.substring(5);
					if(s.startsWith("authors="))
						this.authors = s.substring(8);
					if(s.startsWith("startloc="))
					{
						int b = 0, c = 9;
						CoordSet startLoc = new CoordSet(0, 0, 0);
						for(int i = 9; i < s.length(); i++)
						{
							if(s.charAt(i) == ',' || i == s.length() - 1)
								switch(b)
								{
								case 0:
									startLoc.setX(Double.valueOf(s.substring(c, i)));
									c = i + 1;
									b++;
									break;
								case 1:
									startLoc.setX(Double.valueOf(s.substring(c, i)));
									c = i + 1;
									b++;
									break;
								case 2:
									startLoc.setX(Double.valueOf(s.substring(c)));
									break;
								}
						}
						this.startLocations.add(startLoc); //s.substring(5);
					}
					if(s.startsWith("deathtype="))
						this.collapseType = collapseType.valueOf(s.substring(10).toUpperCase());
					if(s.startsWith("blocktype="))
						this.collapseBlock = Material.valueOf(s.substring(10).toUpperCase());
					if(s.startsWith("uid="))
						this.UID = Integer.valueOf(s.substring(4));
					if(s.startsWith("filename="))
						this.fileName = s.substring(9);
					if(s.startsWith("worldtype="))
						this.worldType = WorldType.valueOf(s.substring(10).toUpperCase());
				}
				bufferedReader.close();
			} catch (Exception e) 
			{
				e.printStackTrace();
			}
		} else
			throw new IllegalArgumentException();
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getAuthors()
	{
		return this.authors;
	}
	
	public List<CoordSet> getStartLocations()
	{
		return this.startLocations;
	}
	
	public CoordSet getSpectatorStartLocation()
	{
		return spectatorStartLocation;
	}
	
	public int getUID()
	{
		return this.UID;
	}
	
	public Material getDeathBlock()
	{
		return collapseBlock;
	}
	
	public CollapseType getDeathType()
	{
		return collapseType;
	}
	
	public WorldType getWorldType()
	{
		return worldType;
	}
	
	public String getFileName()
	{
		return fileName;
	}
	
	public enum WorldType
	{
		HUB, SPAWN, GAMEWORLD;
	}
	
	public enum CollapseType
	{
		COLLAPSE, FLOOD, FIELD;
	}
}
