import java.*;
import java.awt.*;

class CodeFrame extends Frame
{
  TextField stationCount;  
  Checkbox orderIndikator;
  TextField timeAll;  
  TextField stationId;
  TextField minT;
  TextField maxT;
  Button next;
  Button prev;
  Label index;
  Code current;
  int spos;

  public CodeFrame(String title)
  {
    super(title);
    current = new Code();
    current.load(title);
    initElements();
  }

  private void initElements()
  {
    Panel con = new Panel();
    con.setLayout(new GridLayout(0,1));

    Panel p = new Panel();
    p.add(new Label("number of stations"));
    p.add(stationCount = new TextField("",3));
    con.add(p);

    p = new Panel();
    p.add(new Label("worktime"));
    p.add(timeAll = new TextField("",6));
    p.add(new Label("s"));
    con.add(p);

    p = new Panel();
    p.add(orderIndikator = new Checkbox("preserve Order"));
    orderIndikator.setState(current.orderIndicator);
    con.add(p);

    p = new Panel();
    p.setLayout(new GridLayout(1,2,2,0));
    p.add(prev = new Button("previous"));
    p.add(next = new Button("next"));
    con.add(p);

    p = new Panel();
    p.add(new Label("Index : "));
    p.add(index = new Label("1"));
    con.add(p);

    p = new Panel();
    spos = 1;
    p.add(new Label("Station Id"));
    p.add(stationId = new TextField("",2));
    con.add(p);

    p = new Panel();
    p.add(new Label("min time"));
    p.add(minT = new TextField("",6));
    p.add(new Label("s"));
    con.add(p);
    
    p = new Panel();
    p.add(new Label("max time"));
    p.add(maxT = new TextField("",6));
    p.add(new Label("s"));
    con.add(p);

    p = new Panel();
    p.setLayout(new GridLayout(1,2,2,0));
    p.add("South",new Button("Load"));
    p.add("South",new Button("Save"));
    con.add(p);

    p = new Panel();
    p.setLayout(new GridLayout(1,1,2,0));
    p.add("South",new Button("OK"));
    con.add(p);


    add("North",con);

    pack();
    update();

  }


  private void confirm()
  {
        int tmp;
        current.stationCount = Integer.parseInt((stationCount.getText()).trim());
        if (current.stationCount < spos)
          spos = current.stationCount;

        tmp = (int)(1000*(Double.valueOf(timeAll.getText().trim())).doubleValue());
        current.tg = tmp;

        int pos = spos - 1;
        tmp = Integer.parseInt(stationId.getText().trim());
        current.stationId[pos] = tmp;
        current.min[pos] = (int)(1000*(Double.valueOf(minT.getText().trim())).doubleValue());
        current.max[pos] = (int)(1000*(Double.valueOf(maxT.getText().trim())).doubleValue());
  }

  private void update()
  {
      stationCount.setText(String.valueOf(current.stationCount));
      timeAll.setText(Double.toString((((double)(current.tg))/1000)));
      orderIndikator.setState(current.orderIndicator);
      index.setText(String.valueOf(spos));
      index.repaint();
      int pos = spos - 1;
      stationId.setText(String.valueOf(current.stationId[pos]));
      minT.setText(Double.toString((((double)(current.min[pos]))/1000)));
      maxT.setText(Double.toString((((double)(current.max[pos]))/1000)));
      super.repaint();
  }

  public boolean action(Event evt,Object arg)
  {
    if (evt.target instanceof Button) 
    {
      confirm();
      if ("Load".equals(arg))
      {
        current.load(getTitle());
      }
      else if ("Save".equals(arg))
      {
        current.save(getTitle());
      }
      else if ("OK".equals(arg))
      {
        hide();
	return true;
      }
      else if ((spos < current.stationCount)  && "next".equals(arg))
        spos++;
      else if ((spos > 1)  && "previous".equals(arg))
        spos--;

      update();
      return true;
    }

    if (evt.target instanceof Checkbox) 
    {
      current.orderIndicator = ((Checkbox)evt.target).getState();
      return true;
    }


    return false;
  }


  public Code getCode()
  {
    return (Code)current.clone();
  }

}

