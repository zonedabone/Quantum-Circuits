package Ne0nx3r0.QuantumCircuits;

import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Block;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class QuantumCircuitsBlockListener extends BlockListener {
    private final QuantumCircuits plugin;
    private static BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };

    public QuantumCircuitsBlockListener(final QuantumCircuits plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        Block bBlock = event.getBlock();

        for(int i = 0; i < faces.length; i++){
            if(faces[i] == BlockFace.DOWN && (
               bBlock.getFace(faces[i],2).getType() == Material.SIGN_POST
            || bBlock.getFace(faces[i],2).getType() == Material.WALL_SIGN)){
                
                QuantumActivate((Sign) bBlock.getFace(faces[i],2).getState(),event.getOldCurrent(),event.getNewCurrent());

            }else if(bBlock.getFace(faces[i]).getType() == Material.SIGN_POST
                  || bBlock.getFace(faces[i]).getType() == Material.WALL_SIGN){

                QuantumActivate((Sign) bBlock.getFace(faces[i]).getState(),event.getOldCurrent(),event.getNewCurrent());
                
            }
        }
    }

    private static boolean isOn(Block activate){
    	if(activate.getType() == Material.LEVER){
	        int iData = (int) activate.getData();
	
	        if((iData&0x08) == 0x08){
	            return true;
	        }
	        return false;
    	}else{
	        int iData = (int) activate.getData();
	    	
	        if((iData&0x04) == 0x04){
	            return true;
	        }
	        return false;
    	}
    }

    private static void setOn(Block block, Block block2){
        setReceiver(block,block2,true);
    }
    private static void setOff(Block block, Block block2){
        setReceiver(block,block2,false);
    }
    private static void setReceiver(Block block, Block block2,boolean on){
        if(block.getType() == Material.LEVER){

	        int iData = (int) block.getData();
	
	        if(on && (iData&0x08) != 0x08){
	            iData|=0x08;//send power on
	            block.setData((byte) iData);
	        }else if(!on && (iData&0x08) == 0x08){
	            iData^=0x08;//send power off
	            block.setData((byte) iData);
	        }
        }else if(block.getType() == Material.TRAP_DOOR){

	        int iData = (int) block.getData();
	
	        if(on && (iData&0x04) != 0x04){
	            iData|=0x04;//send power on
	            block.setData((byte) iData);
	        }else if(!on && (iData&0x04) == 0x04){
	            iData^=0x04;//send power off
	            block.setData((byte) iData);
	        }
        }else if(block.getType() == Material.IRON_DOOR_BLOCK || block.getType() == Material.WOODEN_DOOR){

	        int iData = (int) block.getData();
	        int iData2 = (int) block2.getData();
	
	        if(on && (iData&0x04) != 0x04){
	            iData|=0x04;//send power on
	            block.setData((byte) iData);
	        }else if(!on && (iData&0x04) == 0x04){
	            iData^=0x04;//send power off
	            block.setData((byte) iData);
	        }
	        if(on && (iData2&0x04) != 0x04){
	            iData2|=0x04;//send power on
	            block2.setData((byte) iData2);
	        }else if(!on && (iData2&0x04) == 0x04){
	            iData2^=0x04;//send power off
	            block2.setData((byte) iData2);
	        }
        }
    }

    private void QuantumActivate(Sign activator,int iOldCurrent,int iNewCurrent){
        String[] sBlockLines = activator.getLines();

        if(sBlockLines[0].equals("") || sBlockLines[1].equals("") || sBlockLines[2].equals("") || sBlockLines[3].equals("")){
            return;
        }

        Block bReceiver;
        Block bReceiver2;

        try{
            bReceiver = activator.getWorld().getBlockAt(Integer.parseInt(sBlockLines[1]),Integer.parseInt(sBlockLines[2]),Integer.parseInt(sBlockLines[3]));
            bReceiver2 = activator.getWorld().getBlockAt(Integer.parseInt(sBlockLines[1]),Integer.parseInt(sBlockLines[2])+1,Integer.parseInt(sBlockLines[3]));
        }
        catch(Exception e){
            return;
        }
        
        // This check runs again in seton/off, but we do it here to filter out broken links
        if(bReceiver.getType() == Material.LEVER || bReceiver.getType() == Material.IRON_DOOR_BLOCK || bReceiver.getType() == Material.WOODEN_DOOR || bReceiver.getType() == Material.TRAP_DOOR){
            if(sBlockLines[0].equalsIgnoreCase("quantum")||sBlockLines[0].equalsIgnoreCase("[quantum]")){
                //makes receiver match source status
            	setReceiver(bReceiver,bReceiver2,iNewCurrent>0);
            }else if(sBlockLines[0].equalsIgnoreCase("qreverse")||sBlockLines[0].equalsIgnoreCase("[qreverse]")){
                //makes receiver match the reverse of source status
            	setReceiver(bReceiver,bReceiver2,!(iNewCurrent>0));
            }else if(sBlockLines[0].equalsIgnoreCase("qtoggle")||sBlockLines[0].equalsIgnoreCase("[qtoggle]")){
                //toggles receiver when powered
                if(iNewCurrent > 0){
                	setReceiver(bReceiver,bReceiver2,!isOn(bReceiver));
                }
            }else if(sBlockLines[0].equalsIgnoreCase("qon")||sBlockLines[0].equalsIgnoreCase("[qon]")){
                //always set on when powered
                if(iNewCurrent > 0){
                    setOn(bReceiver,bReceiver2);
                }
            }else if(sBlockLines[0].equalsIgnoreCase("qoff")||sBlockLines[0].equalsIgnoreCase("[qoff]")){
                //always set off when powered
                if(iNewCurrent > 0){
                    setOff(bReceiver,bReceiver2);
                }
            }else if (sBlockLines[0].length() > 4 && sBlockLines[0].substring(0,4).equalsIgnoreCase("qlag")
            || (sBlockLines[0].length() > 6 && sBlockLines[0].substring(0,5).equalsIgnoreCase("[qlag") && sBlockLines[0].substring(sBlockLines[0].length()-1).equalsIgnoreCase("]"))){
                String[] sLagTimes = sBlockLines[0].split("/");

                boolean powerOn;
                int iLagTime;

                if(iNewCurrent > 0){
                    iLagTime = Integer.parseInt(sLagTimes[1]);
                    powerOn = true;
                }else{
                    iLagTime = Integer.parseInt(sLagTimes[2].substring(0,sLagTimes[2].length()-1));
                    powerOn = false;
                }

                if(iLagTime < 0){
                    iLagTime = 0;
                }else if(iLagTime > plugin.MAX_LAG_TIME){
                    iLagTime = plugin.MAX_LAG_TIME;
                }

                //convert to seconds
                iLagTime = iLagTime * 20;

                plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,new lagSetter(bReceiver,bReceiver2,powerOn),iLagTime);
            }
        }
    }

    private static class lagSetter implements Runnable{
        private final Block blockToChange;
        private final Block blockToChange2;
        private final boolean setPositive;

        lagSetter(Block blockToChange,Block blockToChange2,boolean setPositive){
            this.blockToChange = blockToChange;
            this.blockToChange2 = blockToChange2;
            this.setPositive = setPositive;
        }
        
        public void run(){
            if(this.setPositive){
                setOn(this.blockToChange,this.blockToChange2);
            }else{
                setOff(this.blockToChange,this.blockToChange2);
            }
        }
    }
    
    public void onSignChange(SignChangeEvent event){
    	String[] sLines = event.getLines();
        if(sLines[0].equalsIgnoreCase("quantum")
        || sLines[0].equalsIgnoreCase("[quantum]")
        || sLines[0].equalsIgnoreCase("qreverse")
        || sLines[0].equalsIgnoreCase("[qreverse]")
        || sLines[0].equalsIgnoreCase("qtoggle")
        || sLines[0].equalsIgnoreCase("[qtoggle]")
        || sLines[0].equalsIgnoreCase("qon")
        || sLines[0].equalsIgnoreCase("[qon]")
        || sLines[0].equalsIgnoreCase("qoff")
        || sLines[0].equalsIgnoreCase("[qoff]")
        || (sLines[0].length() > 4 && sLines[0].substring(0,4).equalsIgnoreCase("qlag"))
        || (sLines[0].length() > 6 && sLines[0].substring(0,5).equalsIgnoreCase("[qlag") && sLines[0].substring(sLines[0].length()-1).equalsIgnoreCase("]"))){
        	if ((!QuantumCircuits.permissionHandler.has(event.getPlayer(), "quantum.create"))&&plugin.USE_PERMISSIONS){
        		event.setCancelled(true);
        		event.getBlock().setType(Material.AIR);
        		event.getPlayer().getInventory().addItem(new ItemStack(Material.SIGN,1));
        		event.getPlayer().sendMessage(ChatColor.RED+"You don't have permission to create Quantum signs.");
        	}else{
        		event.setLine(1, "");
        		event.setLine(2, "");
        		event.setLine(3, "");
        	}
        }
    }
}
