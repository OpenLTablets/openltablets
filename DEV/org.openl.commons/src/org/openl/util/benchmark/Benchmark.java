package org.openl.util.benchmark;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.openl.util.Log;


public class Benchmark
{
    BenchmarkUnit[] _units;

    public Benchmark(BenchmarkUnit[] units)
    {
      _units = units;
    }

    BenchmarkUnit findUnit(String name)
    {
      for(int i = 0; i < _units.length; ++i)
      {
        if (_units[i].getName().equals(name))
          return _units[i];
      }

      throw new RuntimeException("Unit " + name + " not found");
    }


    HashMap _measurements = null;

    public Map measureAll(int ms) throws Exception
    {
      _measurements = new HashMap();
      for(int i = 0; i < _units.length; ++i)
      {
        measureUnit(_units[i], ms);
      }

      return _measurements;
    }

    public BenchmarkInfo measureUnit(String name, int ms) throws Exception
    {
      return measureUnit(findUnit(name), ms);
    }


    public void profileUnit(String name, int times) throws Exception
    {
      if (_measurements == null)
        _measurements = new HashMap();

      BenchmarkUnit bu = findUnit(name);
      satisfyPreconditions(bu);

      bu.millisecondsToRun(times == -1 ? 1 : times);
    }




    public BenchmarkInfo measureUnit(BenchmarkUnit bu, int ms) throws Exception
    {

      Log.info("Benchmarking Unit " + bu.getName());

      if (_measurements == null)
        _measurements = new HashMap();

      satisfyPreconditions(bu);
      return runUnit(bu, ms, false);
    }

    public void satisfyPreconditions(BenchmarkUnit bu) throws Exception
    {
      String[] names = bu.performAfter();
      for(int i = 0; i < names.length; ++i)
      {
        BenchmarkUnit prev = findUnit(names[i]);
        BenchmarkInfo bi = (BenchmarkInfo) _measurements.get(bu.getName());
        if (bi == null)
        {
          satisfyPreconditions(prev);
          runUnit(prev, 1,true);
        }

      }

    }

    public BenchmarkInfo runUnit(BenchmarkUnit bu, int ms, boolean once) throws Exception
    {
    	
      if (_measurements == null)
        _measurements = new HashMap();

      BenchmarkInfo bi = (BenchmarkInfo) _measurements.get(bu.getName());

      if (bi == null)
      {
        bi = new BenchmarkInfo(null, bu, bu.getName());
 
        bi.firstRunms = bu.millisecondsToRun();
        _measurements.put(bu.getName(), bi);
      }

      if (once)
        return bi;

      RunInfo info = makeRun(bu, bu.getMinRuns(), ms);
      bi.runs.add(info);
      return bi;
    }



    RunInfo makeRun(BenchmarkUnit bu, int minRuns, int ms) throws Exception
    {

      int minMillis = ms == -1 ? bu.getMinms()  : ms;
      int runs = minRuns;
      while(true)
      {
        long time = bu.millisecondsToRun(runs);
        if (time > minMillis)
        {
          return new RunInfo(runs, time);
        }

        if (time <= 0)
          time = 1;

        double mult = Math.min(200, ((double) minMillis) * 1.1 / time);


        int newRuns = (int)Math.ceil(runs * mult);
        runs = Math.max(runs + 1, newRuns);

      }
    }


    public void printResult(Map map, PrintStream ps )
    {
      for(int i = 0; i < _units.length; ++i)
      {
        BenchmarkInfo info = (BenchmarkInfo)map.get(_units[i].getName());
        ps.println(info);
      }
    }


    public String makeTableRow(Map map, String separator)
    {
      String res ="";
      for(int i = 0; i < _units.length; ++i)
      {
        BenchmarkInfo info = (BenchmarkInfo)map.get(_units[i].getName());
        res += BenchmarkInfo.printDouble(info.avg()) + separator;
      }

      return res;
    }

    public String makeTableHeader(String separator)
    {
      String res ="";
      for(int i = 0; i < _units.length; ++i)
      {
        res += _units[i].getName() + separator;
      }
      return res;
    }



}