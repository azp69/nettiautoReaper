import java.net.*;
import java.io.*;
import java.util.*;

class Nettiauto
{
  public static void main(String args[]) throws Exception {

  long alkuAika = System.currentTimeMillis();
  int alkusivu;
  int loppusivu;

  if (args.length == 3)
  {
    if (args[0].equals("-get"))
    {
      alkusivu = Integer.parseInt(args[1]);
      loppusivu = Integer.parseInt(args[2]);
      for (int i = alkusivu; i < loppusivu; i++)
      {
        HaeDataJaTallennaTiedostoon(i);
      }
    }
    else if (args[0].equals("-put"))
    {
      for (int i = Integer.parseInt(args[1]); i < Integer.parseInt(args[2]); i++)
      {
        LataaDataTiedostosta("nettiauto_dump_page" + i + ".txt");
      }
    }
  }

  long timeNow = System.currentTimeMillis();

  System.out.println("Tietojen lataamiseen meni " + ((System.currentTimeMillis() - alkuAika) / 1000) + "s");
  System.out.println("Tietojen kaivamiseen meni " + (System.currentTimeMillis() - timeNow) + "ms.");

  }

  public static void LataaDataTiedostosta(String tiedosto) throws Exception
  {
    Reaper r = new Reaper();
    String everything;

    try(BufferedReader br = new BufferedReader(new FileReader(tiedosto))) {
      StringBuilder sb = new StringBuilder();
      String line = br.readLine();

      while (line != null) {
          sb.append(line);
          sb.append(System.lineSeparator());
          line = br.readLine();
      }
      everything = sb.toString();
    }

    r.Parseri(everything);

  }

  public static void HaeDataJaTallennaTiedostoon(int sivu) throws Exception
  {
    Yhteys y = new Yhteys();
    try {

      String url = "https://www.nettiauto.com/vaihtoautot?page=" + sivu;
      String tmp = y.HaeData(url);

      Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("nettiauto_dump_page" + sivu + ".txt"), "utf-8"));
      writer.write(tmp);
      writer.close();

      }
      catch (UnknownHostException e) {
        System.out.println("Väärä osoite");
      }
  }
}

class Auto
{
  public String merkki, malli;
  public int vuosimalli, kilometrit, hinta;
}

class Reaper
{
  private int indeksilaskuri;
  public int autoja = 0;

  public void Parseri(String in)
  {
    indeksilaskuri = 0;
    // String[] etsittavat = new String[] { "data-make=\"", "data-model=\"", "data-vtype=\"", "data-year=\"", "data-mileage=\"", "data-price=\"" };
    String[] etsittavat = new String[] { "data-make=\"", "data-model=\"", "data-year=\"", "data-mileage=\"", "data-price=\"" };

    String sivumaaraA = "<span class=\"totPage\">";
    String sivumaaraL = "</span>";

    int sivumaaraAlkaa = in.indexOf(sivumaaraA) + sivumaaraA.length();
    int sivumaaraLoppuu = in.indexOf(sivumaaraL, sivumaaraAlkaa);

    String sivuja = in.substring(sivumaaraAlkaa, sivumaaraLoppuu);

    /*
    System.out.println(sivuja);
    System.out.println(sivumaaraA);
    System.out.println(sivumaaraAlkaa);
    */

    boolean etsitaan = true;
    int laskuri = 0;

    try
    {
    
    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("autot.txt", true)));

    while (etsitaan)
    {
      int i = 0;
      for (String e : etsittavat)
      {
        String etsittava = EtsiArvo(in, e, "\"", indeksilaskuri);
        if (!etsittava.equals(""))
        {
          out.print(etsittava + ";");
          System.out.print(etsittava + "\t");
        }
        else
        {
          System.out.println("Loppu");
          etsitaan = false;
          break;
        }
        autoja++;
      }
        System.out.println();
        out.println("");
        laskuri++;
        if (laskuri > 29)
        etsitaan = false;
    }
    out.close();
  }
  catch (Exception e)
  {
    System.out.println(e);
  }


  }

  public String EtsiArvo(String data, String etsittavaA, String etsittavaL, int aloitusindeksi)
  {
    int _etsittavaAlkaa = data.indexOf(etsittavaA, indeksilaskuri) + etsittavaA.length();
    int _etsittavaLoppuu = data.indexOf(etsittavaL, _etsittavaAlkaa);
    String _etsittava = data.substring(_etsittavaAlkaa, _etsittavaLoppuu);
    if (indeksilaskuri > _etsittavaLoppuu)
    {
      return "";
    }
    else
    {
      indeksilaskuri = _etsittavaLoppuu;
      return _etsittava;
    }
  }
}

class Yhteys
{
  public String HaeData(String osoite) throws Exception
  {
    int yrityksia = 0;
    int montakoKertaaKokeillaan = 3;

    while (true)
    {
      try{
        long startTime = System.currentTimeMillis();
        int c;
        URL hp = new URL(osoite);
        System.out.println("Muodostetaan yhteyttä palvelimeen...");
        HttpURLConnection hpCon = (HttpURLConnection) hp.openConnection();
        hpCon.setFollowRedirects(true);
        hpCon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");
        InputStream input = hpCon.getInputStream();
        String tmp = "";
        System.out.println("Ladataan sivua " + osoite + "...");
        while (((c = input.read()) != -1)) {
          tmp = tmp + (char) c;
        }
        input.close();
        long timeNow = System.currentTimeMillis();
        System.out.println("Valmis. Hakuun meni " + (timeNow - startTime) + "ms.");
        return tmp;
      }
      catch (Exception e)
      {
        yrityksia++;
        if (yrityksia > montakoKertaaKokeillaan)
        {
          System.exit(1);
        }
        else
        {
          System.out.println("Virhe " + e + "\nYritetään uudelleen...");
        }
      }
    }
  }
}
